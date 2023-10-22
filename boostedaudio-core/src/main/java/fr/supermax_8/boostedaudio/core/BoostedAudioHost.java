package fr.supermax_8.boostedaudio.core;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.utils.FileUtils;
import fr.supermax_8.boostedaudio.core.utils.ResourceUtils;
import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfiguration;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
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
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BoostedAudioHost {

    private static BoostedAudioHost instance;


    private final BoostedAudioConfiguration configuration;
    private AudioWebSocketServer webSocketServer;
    private Undertow webServer;
    private SSLContext sslContext;
    private SSLContext dummySslContext;
    private File webserver;
    private File audioDir;
    private boolean sucessfulSetup = false;

    public BoostedAudioHost(BoostedAudioConfiguration configuration) {
        instance = this;
        this.configuration = configuration;
        load();
    }

    public void load() {
        BoostedAudioAPI.api.info("Plugin load...");
        CompletableFuture.runAsync(() -> {
            try {
                startServers();
            } catch (Throwable ex) {
                BoostedAudioAPI.api.debug("Error while starting servers");
                if (configuration.isDebugMode()) ex.printStackTrace();
            }
        });

        BoostedAudioAPI.api.info("Plugin loaded");
    }

    public AudioWebSocketServer getWebSocketServer() {
        return webSocketServer;
    }

    public File getAudioDir() {
        return audioDir;
    }

    public File getWebserver() {
        return webserver;
    }

    private void startServers() throws IOException {
        BoostedAudioAPI.api.info("Starting servers...");
        initSSL();
        File webserver = new File(configuration.getDataFolder(), "webhost");
        File index = new File(webserver, "index.html");
        if (!configuration.isCustomClient()) {
            ResourceUtils.saveResource("webhost/index.html", new File(configuration.getDataFolder(), "webhost" + File.separator + "index.html").getAbsolutePath());

            String ipConnec = configuration.getClientWebSocketLink();

            if (ipConnec == null || ipConnec.isEmpty()) {
                String ip = configuration.getWebSocketHostName().replace("localhost", getPublicIp());
                BoostedAudioAPI.api.debug("PublicIP: " + ip);
                ipConnec = (sslContext != null ? "wss://" : "ws://") +
                        ip +
                        ":" + configuration.getWebSocketPort();
            }

            BoostedAudioAPI.api.debug("WebSocket IP: " + ipConnec);
            FileUtils.replaceInFile(index, "%WS_IP%", ipConnec);
            FileUtils.replaceInFile(index, "let proximityChat = true;", "let proximityChat = " + configuration.isVoiceChatEnabled());
        }

        if (index.exists()) {
            List<String> placeholders;
            if (BoostedAudioLoader.isPremium()) placeholders = configuration.getClientConfig();
            else {
                Reader reader = new InputStreamReader(ResourceUtils.getResourceAsStream("config.yml"));
                CrossConfiguration fc = CrossConfiguration.newConfig().load(reader);
                placeholders = (List<String>) fc.get("clientConfig");
            }

            placeholders.forEach(s -> {
                String[] placeholderEntry = s.split("=", 2);
                FileUtils.replaceInFile(index, placeholderEntry[0], placeholderEntry[1]);
            });
        }

        if (configuration.isAutoHost())
            try {
                CompletableFuture.runAsync(this::startSelfHostWebServer);
            } catch (Exception e) {
                if (configuration.isDebugMode()) e.printStackTrace();
            }

        BoostedAudioAPI.api.debug("Starting WebSocket server...");
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
                        BoostedAudioAPI.api.debug("LAN : " + realIp);
                    }
                }
            }
        } catch (Exception e) {
            if (configuration.isDebugMode()) e.printStackTrace();
        }

        String ip = configuration.getWebSocketHostName().replace("localhost", realIp);
        InetSocketAddress inet = new InetSocketAddress(ip, configuration.getWebSocketPort());
        BoostedAudioAPI.api.debug("unresolved" + inet.isUnresolved());
        BoostedAudioAPI.api.debug("websocket " + inet);
        webSocketServer = new AudioWebSocketServer(inet);
        webSocketServer.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
        webSocketServer.setReuseAddr(true);
        webSocketServer.setTcpNoDelay(true);
        CompletableFuture.runAsync(() -> {
            webSocketServer.run();
        });
        BoostedAudioAPI.api.info("BoostedAudio Host setuped !");
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
        webserver = new File(configuration.getDataFolder(), "webhost");
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
                .build();

        webServer.start();

        audioDir.mkdirs();

        BoostedAudioAPI.api.debug("SelfHostWebServer started with port " + port);
    }

    private void initSSL() {
        BoostedAudioAPI.api.debug("Init SSL...");
        try {
            BoostedAudioAPI.api.debug("SSL setup...");

            ResourceUtils.saveResource("default.jks", new File(configuration.getDataFolder(), "default.jks").getAbsolutePath());
            dummySslContext = getSSLContext(Files.newInputStream(new File(configuration.getDataFolder(), "default.jks").toPath()), "boostedaudio".toCharArray());
            if (configuration.isSsl()) {
                sslContext = getSSLContext(
                        Files.newInputStream(new File(configuration.getDataFolder(), configuration.getKeystoreFileName()).toPath()),
                        configuration.getKeystorePassword().toCharArray()
                );
            } else sslContext = dummySslContext;

            BoostedAudioAPI.api.debug("SSL setuped");
        } catch (Exception e) {
            BoostedAudioAPI.api.debug("ERROR");
            if (configuration.isDebugMode()) e.printStackTrace();
        }
        BoostedAudioAPI.api.debug("SSL Init...");
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

    public boolean isSucessfulSetup() {
        return sucessfulSetup;
    }

    /**
     * You can use it only if execute on a host server
     *
     * @return the instance of the host or null if on a diffuser server
     */
    public static BoostedAudioHost getInstance() {
        return instance;
    }


}