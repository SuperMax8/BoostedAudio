package fr.supermax_8.boostedaudio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.supermax_8.boostedaudio.commands.AudioCommand;
import fr.supermax_8.boostedaudio.commands.BoostedAudioCommand;
import fr.supermax_8.boostedaudio.ingame.VocalLinker;
import fr.supermax_8.boostedaudio.utils.AroundManager;
import fr.supermax_8.boostedaudio.utils.FileUtils;
import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.PacketList;
import fr.supermax_8.boostedaudio.web.packets.RTCIcePacket;
import io.undertow.Undertow;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
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

public class BoostedAudio extends JavaPlugin {

    private static BoostedAudio instance;

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

    @Override
    public void onEnable() {
        instance = this;
        aroundManager = new AroundManager();

        try {
            NumberFormat f = NumberFormat.getInstance();
            bukkitVersion = f.parse(Bukkit.getBukkitVersion()).doubleValue();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        configuration = new BoostedAudioConfiguration();
        getCommand("audio").setExecutor(new AudioCommand());
        getCommand("boostedaudio").setExecutor(new BoostedAudioCommand());

        CompletableFuture.runAsync(this::startServers);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, s -> {
            if (webSocketServer != null && webSocketServer.isOpen()) {
                new VocalLinker().runTaskTimerAsynchronously(this, 0, 0);
                s.cancel();
            }
        }, 0, 0);
    }

    @Override
    public void onDisable() {
        CompletableFuture.runAsync(() -> {
            try {
                webSocketServer.stop();
                if (webServer != null) webServer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void info(String message) {
        instance.getLogger().info(message);
    }

    public static void debug(String message) {
        if (instance.configuration.isDebugMode()) info(message);
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

    private void startServers() {
        if (configuration.isSsl()) initSSL();

        File webserver = new File(getDataFolder(), "webhost");
        saveResource("webhost/index.html", true);

        String ip = (configuration.isSsl() ? "wss://" : "ws://") +
                configuration.getWebSocketHostName() +
                ":" + configuration.getWebSocketPort();

        debug("WebSocket IP: " + ip);
        File index = new File(webserver, "index.html");
        FileUtils.replaceInFile(index, "%WS_IP%", ip);
        FileUtils.replaceInFile(index, "let proximityChat = true;", "let proximityChat = " + configuration.isVoiceChatEnabled());

        if (configuration.isAutoHost())
            try {
                Bukkit.getScheduler().runTaskAsynchronously(this, this::startSelfHostWebServer);
            } catch (Exception e) {
                e.printStackTrace();
            }


        webSocketServer = new AudioWebSocketServer(new InetSocketAddress(configuration.getWebSocketHostName(), configuration.getWebSocketPort()));
        if (sslContext != null) {
            debug("WebSocket will be open with ssl");
            webSocketServer.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
        }
        webSocketServer.setReuseAddr(true);
        CompletableFuture.runAsync(() -> {
            webSocketServer.run();
        });
    }

    private void startSelfHostWebServer() {
        int port = configuration.getAutoHostPort();

        // Spécifiez le répertoire où se trouvent les fichiers à servir
        File webserver = new File(getDataFolder(), "webhost");

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
                .build();

        webServer.start();

        debug("SelfHostWebServer started with port " + port);
    }

    private void initSSL() {
        try {
            char[] keystorePassword = configuration.getKeystorePassword().toCharArray();
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(Files.newInputStream(new File(getDataFolder(), configuration.getKeystoreFileName()).toPath()), keystorePassword);

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