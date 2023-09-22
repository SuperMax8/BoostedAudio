package fr.supermax_8.boostedaudio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.supermax_8.boostedaudio.commands.AudioCommand;
import fr.supermax_8.boostedaudio.commands.BoostedAudioCommand;
import fr.supermax_8.boostedaudio.ingame.AudioManager;
import fr.supermax_8.boostedaudio.ingame.PlayerListener;
import fr.supermax_8.boostedaudio.utils.AroundManager;
import fr.supermax_8.boostedaudio.utils.FileUtils;
import fr.supermax_8.boostedaudio.utils.TemporaryListener;
import fr.supermax_8.boostedaudio.utils.UpdateChecker;
import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.PacketList;
import fr.supermax_8.boostedaudio.web.packets.RTCIcePacket;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.xnio.Options;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.file.Files;
import java.security.KeyStore;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BoostedAudio {

    private static BoostedAudio instance;
    private static BoostedAudioLoader loader;

    public static double bukkitVersion;
    public static String VERSION;

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(PacketList.class, new PacketList.Adapter())
            .registerTypeAdapter(RTCIcePacket.class, new RTCIcePacket.Adapter())
            .create();

    private AudioWebSocketServer webSocketServer;
    private Undertow webServer;
    private SSLContext sslContext;
    private SSLContext dummySslContext;
    private BoostedAudioConfiguration configuration;
    private AroundManager aroundManager;
    private AudioManager audioManager;
    private File webserver;
    private File audioDir;

    private boolean sucessfulSetup = false;

    private LinkedList<Listener> listeners;

    public void onEnable() {
        BoostedAudio.instance = this;
        loader = BoostedAudioLoader.getInstance();
        VERSION = loader.getDescription().getVersion();
        listeners = new LinkedList<>();


        try {
            NumberFormat f = NumberFormat.getInstance();
            bukkitVersion = f.parse(Bukkit.getBukkitVersion()).doubleValue();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        reload();

        loader.getCommand("audio").setExecutor(new AudioCommand());
        loader.getCommand("boostedaudio").setExecutor(new BoostedAudioCommand());
    }

    public void reload() {
        info("Plugin load...");
        stop();
        aroundManager = new AroundManager();
        configuration = new BoostedAudioConfiguration();
        CompletableFuture.runAsync(this::startServers);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (webSocketServer != null && webSocketServer.isOpen()) {
                    cancel();
                    audioManager = new AudioManager();
                    audioManager.runTaskTimerAsynchronously(loader, 0, 0);
                    PlayerListener playerListener = new PlayerListener();
                    loader.getServer().getPluginManager().registerEvents(playerListener, loader);
                    listeners.add(playerListener);
                    Bukkit.getOnlinePlayers().forEach(player -> playerListener.join(new PlayerJoinEvent(player, null)));
                }
            }
        }.runTaskTimerAsynchronously(loader, 0, 0);

        info("Plugin loaded");

        try {
            if (!configuration.isNotification()) return;
            BoostedAudio.info("Checking for updates...");
            new UpdateChecker(BoostedAudioLoader.getInstance(), 112747).getVersion(v -> {
                if (v.equals(VERSION)) return;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        info("§aNew version available : §6" + v + " §ayou are on §7" + VERSION);
                        new TemporaryListener<PlayerJoinEvent>(PlayerJoinEvent.class, EventPriority.NORMAL, event -> {
                            Player p = event.getPlayer();
                            if (p.hasPermission("boostedaudio.admin")) {
                                p.sendMessage("§2[BoostedAudio] §aNew version available : §e" + v + " §ayou are on §e" + VERSION);
                                return true;
                            }
                            return false;
                        });
                    }
                }.runTask(BoostedAudioLoader.getInstance());
            });

        } catch (Exception ignored) {
        }
    }

    private void stop() {
        listeners.clear();
        if (aroundManager != null) aroundManager.cancel();
        if (audioManager != null) audioManager.cancel();
        HandlerList.unregisterAll(BoostedAudioLoader.getInstance());
        try {
            /*if (webSocketServer != null && webSocketServer.isOpen()) webSocketServer.stop();
            if (webServer != null) {
                debug("Stopping undertow...");
                webServer.stop();
            }*/
        } catch (Exception e) {
            /*e.printStackTrace();*/
        }
    }

    public void onDisable() {
        long ts = System.currentTimeMillis();
        stop();
        long ts2 = System.currentTimeMillis();
        debug("Servers stopped in " + (ts2 - ts) + " ms");
    }

    public static void info(String message) {
        String finalMessage = "§8§l[§9§lBoostedAudio§8§l] §7" + message;
        Bukkit.getConsoleSender().sendMessage(finalMessage);
    }

    public static void debug(String message) {
        if (instance.configuration.isDebugMode()) info("BoostedAudioDebug: " + message);
    }

    public static BoostedAudio getInstance() {
        return instance;
    }

    public static Gson getGson() {
        return gson;
    }

    public AudioWebSocketServer getWebSocketServer() {
        return webSocketServer;
    }

    public BoostedAudioConfiguration getConfiguration() {
        return configuration;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public AroundManager getAroundManager() {
        return aroundManager;
    }

    public File getAudioDir() {
        return audioDir;
    }

    public File getWebserver() {
        return webserver;
    }

    private void startServers() {
        info("Starting servers...");
        initSSL();
        File webserver = new File(loader.getDataFolder(), "webhost");
        File index = new File(webserver, "index.html");
        if (!configuration.isCustomClient()) {
            loader.saveResource("webhost/index.html", true);

            String ipConnec = configuration.getClientWebSocketLink();

            if (ipConnec == null || ipConnec.isEmpty()) {
                String ip = configuration.getWebSocketHostName().replace("localhost", getPublicIp());
                debug("PublicIP: " + ip);
                ipConnec = (sslContext != null ? "wss://" : "ws://") +
                        ip +
                        ":" + configuration.getWebSocketPort();
            }

            debug("WebSocket IP: " + ipConnec);
            FileUtils.replaceInFile(index, "%WS_IP%", ipConnec);
            FileUtils.replaceInFile(index, "let proximityChat = true;", "let proximityChat = " + configuration.isVoiceChatEnabled());
        }

        if (index.exists()) {
            List<String> placeholders;
            if (isPremium()) placeholders = configuration.getClientConfig();
            else {
                Reader reader = new InputStreamReader(BoostedAudioLoader.getInstance().getResource("config.yml"));
                FileConfiguration fc = YamlConfiguration.loadConfiguration(reader);
                placeholders = fc.getStringList("clientConfig");
            }

            placeholders.forEach(s -> {
                String[] placeholderEntry = s.split("=", 2);
                FileUtils.replaceInFile(index, placeholderEntry[0], placeholderEntry[1]);
            });
        }

        if (configuration.isAutoHost())
            try {
                Bukkit.getScheduler().runTaskAsynchronously(loader, this::startSelfHostWebServer);
            } catch (Exception e) {
                if (BoostedAudio.getInstance().getConfiguration().isDebugMode()) e.printStackTrace();
            }

        debug("Starting WebSocket server...");
        String realIp = "localhost";
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address.isSiteLocalAddress()) {
                        realIp = address.getHostAddress();
                        debug("LAN : " + realIp);
                    }
                }
            }
        } catch (Exception e) {
            if (BoostedAudio.getInstance().getConfiguration().isDebugMode()) e.printStackTrace();
        }

        String ip = configuration.getWebSocketHostName().replace("localhost", realIp);
        InetSocketAddress inet = new InetSocketAddress(ip, configuration.getWebSocketPort());
        debug("unresolved" + inet.isUnresolved());
        debug("websocket " + inet);
        webSocketServer = new AudioWebSocketServer(inet);
        webSocketServer.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
        webSocketServer.setReuseAddr(true);
        webSocketServer.setTcpNoDelay(true);
        CompletableFuture.runAsync(() -> {
            webSocketServer.run();
        });
        sucessfulSetup = true;
    }

    private String getPublicIp() {
        try {
            String urlString = "https://checkip.amazonaws.com/";
            URL url = new URL(urlString);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
                return br.readLine();
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void startSelfHostWebServer() {
        int port = configuration.getAutoHostPort();
        webserver = new File(loader.getDataFolder(), "webhost");
        audioDir = new File(webserver, "audio");
        FileResourceManager resourceManager = new FileResourceManager(webserver);
        ResourceHandler resourceHandler = new ResourceHandler(resourceManager);

        Undertow.Builder builder = Undertow.builder();
        if (configuration.isSsl() && sslContext != null) builder.addHttpsListener(port, "0.0.0.0", sslContext);
        else builder.addHttpsListener(port, "0.0.0.0", dummySslContext);
        webServer = builder
                .setHandler(resourceHandler)
                .setServerOption(Options.REUSE_ADDRESSES, true)
                .setServerOption(UndertowOptions.SHUTDOWN_TIMEOUT, 0)
                /*.setServerOption(UndertowOptions., 0)*/
                .build();

        webServer.start();

        audioDir.mkdirs();

        debug("SelfHostWebServer started with port " + port);
    }

    private void initSSL() {
        debug("Init SSL...");
        try {
            debug("SSL setup...");

            loader.saveResource("default.jks", true);
            dummySslContext = getSSLContext(Files.newInputStream(new File(loader.getDataFolder(), "default.jks").toPath()), "boostedaudio".toCharArray());
            if (configuration.isSsl()) {
                sslContext = getSSLContext(
                        Files.newInputStream(new File(loader.getDataFolder(), configuration.getKeystoreFileName()).toPath()),
                        configuration.getKeystorePassword().toCharArray()
                );
            } else sslContext = dummySslContext;

            debug("SSL setuped");
        } catch (Exception e) {
            debug("ERROR");
            if (BoostedAudio.getInstance().getConfiguration().isDebugMode()) e.printStackTrace();
        }
        debug("SSL Init...");
    }

    private SSLContext getSSLContext(InputStream stream, char[] password) {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");

            keyStore.load(stream, password);

            // Configuration du gestionnaire de clés
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, password);

            // Configuration du contexte SSL
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);
            return sslContext;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isPremium() {
        return false;
    }

    public boolean isSucessfulSetup() {
        return sucessfulSetup;
    }

}