package fr.supermax_8.boostedaudio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import fr.supermax_8.boostedaudio.commands.AudioCommand;
import fr.supermax_8.boostedaudio.utils.FileUtils;
import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.PacketList;
import fr.supermax_8.boostedaudio.web.packets.RTCIcePacket;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.security.KeyStore;
import java.util.concurrent.CompletableFuture;

public class BoostedAudio extends JavaPlugin {

    private static BoostedAudio instance;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(PacketList.class, new PacketList.Adapter())
            .registerTypeAdapter(RTCIcePacket.class, new RTCIcePacket.Adapter())
            .create();

    private AudioWebSocketServer webSocketServer;
    private HttpServer selfWebServer;
    private SSLContext sslContext;
    private BoostedAudioConfiguration configuration;

    @Override
    public void onEnable() {
        instance = this;
        configuration = new BoostedAudioConfiguration();
        getCommand("audio").setExecutor(new AudioCommand());

        CompletableFuture.runAsync(this::startServers);
    }

    @Override
    public void onDisable() {
        try {
            webSocketServer.stop();
            if (selfWebServer != null) selfWebServer.stop(0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
        if (configuration.isAutoHost())
            try {
                startSelfHostWebServer();
            } catch (Exception e) {
                e.printStackTrace();
            }


        webSocketServer = new AudioWebSocketServer(new InetSocketAddress(configuration.getWebSocketHostName(), configuration.getWebSocketPort()));
        if (sslContext != null) {
            debug("WebSocket will be open with ssl");
            webSocketServer.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
        }
        webSocketServer.setReuseAddr(true);
        CompletableFuture.runAsync(() -> webSocketServer.run());
    }

    private void startSelfHostWebServer() throws IOException {
        int port = configuration.getAutoHostPort();

        if (configuration.isSsl()) {
            selfWebServer = HttpsServer.create(new InetSocketAddress(port), 0);
            HttpsServer server = (HttpsServer) selfWebServer;

            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        SSLContext context = getSSLContext();
                        SSLEngine engine = context.createSSLEngine();
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());
                        SSLParameters sslParameters = context.getDefaultSSLParameters();
                        params.setSSLParameters(sslParameters);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else selfWebServer = HttpServer.create(new InetSocketAddress(port), 0);

        File webserver = new File(getDataFolder(), "webclient");
        saveResource("webclient/index.html", true);

        String ip = (configuration.isSsl() ? "wss://" : "ws://") +
                configuration.getWebSocketHostName() +
                ":" + configuration.getWebSocketPort();

        debug("WebSocket IP: " + ip);

        FileUtils.replaceInFile(new File(webserver, "index.html"), "%WS_IP%", ip);

        selfWebServer.createContext("/", exchange -> {
            byte[] responseBytes = Files.readAllBytes(new File(webserver, "index.html").toPath());

            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();

            os.write(responseBytes);
            os.close();
        });
        selfWebServer.start();
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