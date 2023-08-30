package fr.supermax_8.boostedaudio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.supermax_8.boostedaudio.commands.AudioCommand;
import fr.supermax_8.boostedaudio.commands.BoostedAudioCommand;
import fr.supermax_8.boostedaudio.ingame.AudioManager;
import fr.supermax_8.boostedaudio.ingame.PlayerListener;
import fr.supermax_8.boostedaudio.utils.AroundManager;
import fr.supermax_8.boostedaudio.utils.FileUtils;
import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.PacketList;
import fr.supermax_8.boostedaudio.web.packets.RTCIcePacket;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.xnio.Options;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.security.KeyStore;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.concurrent.CompletableFuture;

public class BoostedAudio {

    private static BoostedAudio instance;
    private static BoostedAudioLoader loader;

    public static double bukkitVersion;

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(PacketList.class, new PacketList.Adapter())
            .registerTypeAdapter(RTCIcePacket.class, new RTCIcePacket.Adapter())
            .create();

    private AudioWebSocketServer webSocketServer;
    private Undertow webServer;
    private SSLContext sslContext;
    private BoostedAudioConfiguration configuration;
    private AroundManager aroundManager;
    private AudioManager audioManager;
    private File webserver;
    private File audioDir;

    public void onEnable() {
        BoostedAudio.instance = this;
        loader = BoostedAudioLoader.getInstance();
        aroundManager = new AroundManager();

        try {
            NumberFormat f = NumberFormat.getInstance();
            bukkitVersion = f.parse(Bukkit.getBukkitVersion()).doubleValue();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        configuration = new BoostedAudioConfiguration();
        loader.getCommand("audio").setExecutor(new AudioCommand());
        loader.getCommand("boostedaudio").setExecutor(new BoostedAudioCommand());

        CompletableFuture.runAsync(this::startServers);

        Bukkit.getScheduler().runTaskTimerAsynchronously(loader, s -> {
            if (webSocketServer != null && webSocketServer.isOpen()) {
                s.cancel();
                audioManager = new AudioManager();
                audioManager.runTaskTimerAsynchronously(loader, 0, 0);
                PlayerListener playerListener = new PlayerListener();
                loader.getServer().getPluginManager().registerEvents(playerListener, loader);
                Bukkit.getOnlinePlayers().forEach(player -> playerListener.join(new PlayerJoinEvent(player, null)));
            }
        }, 0, 0);
    }

    public void onDisable() {
        /*audioManager.saveData();*/
        long ts = System.currentTimeMillis();
        try {
            webSocketServer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (webServer != null) {
            debug("Stopping undertow...");
            webServer.stop();
        }
        long ts2 = System.currentTimeMillis();
        debug("Servers stopped in " + (ts2 - ts) + " ms");
    }

    public static void info(String message) {
        loader.getLogger().info(message);
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
        if (configuration.isSsl()) initSSL();

        File webserver = new File(loader.getDataFolder(), "webhost");
        File index = new File(webserver, "index.html");
        if (!configuration.isCustomClient()) {
            loader.saveResource("webhost/index.html", true);

            String ip = (configuration.isSsl() ? "wss://" : "ws://") +
                    configuration.getWebSocketHostName() +
                    ":" + configuration.getWebSocketPort();

            debug("WebSocket IP: " + ip);
            FileUtils.replaceInFile(index, "%WS_IP%", ip);
            FileUtils.replaceInFile(index, "let proximityChat = true;", "let proximityChat = " + configuration.isVoiceChatEnabled());
        }

        if (index.exists()) configuration.getClientConfig().forEach(s -> {
            String[] placeholderEntry = s.split("=", 2);
            FileUtils.replaceInFile(index, placeholderEntry[0], placeholderEntry[1]);
        });

        if (configuration.isAutoHost())
            try {
                Bukkit.getScheduler().runTaskAsynchronously(loader, this::startSelfHostWebServer);
            } catch (Exception e) {
                e.printStackTrace();
            }


        webSocketServer = new AudioWebSocketServer(new InetSocketAddress(configuration.getWebSocketHostName(), configuration.getWebSocketPort()));
        if (sslContext != null) {
            debug("WebSocket will be open with ssl");
            webSocketServer.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
        }
        webSocketServer.setReuseAddr(true);
        webSocketServer.setTcpNoDelay(true);
        CompletableFuture.runAsync(() -> {
            webSocketServer.run();
        });
    }

    private void startSelfHostWebServer() {
        int port = configuration.getAutoHostPort();
        webserver = new File(loader.getDataFolder(), "webhost");
        audioDir = new File(webserver, "audio");
        // Spécifiez le répertoire où se trouvent les fichiers à servir


        // Créez un gestionnaire de ressources pour le répertoire spécifié
        FileResourceManager resourceManager = new FileResourceManager(webserver);

        // Créez un gestionnaire de ressources pour gérer les demandes de fichiers
        ResourceHandler resourceHandler = new ResourceHandler(resourceManager);

        Undertow.Builder builder = Undertow.builder();
        if (configuration.isSsl()) builder.addHttpsListener(port, "0.0.0.0", sslContext);
        else builder.addHttpListener(port, "0.0.0.0");
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
        try {
            char[] keystorePassword = configuration.getKeystorePassword().toCharArray();
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(Files.newInputStream(new File(loader.getDataFolder(), configuration.getKeystoreFileName()).toPath()), keystorePassword);

            // Configuration du gestionnaire de clés
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, keystorePassword);

            // Configuration du contexte SSL
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}