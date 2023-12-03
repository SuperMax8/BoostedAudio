package fr.supermax_8.boostedaudio.spigot.diffuser;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.serverpacket.ServerPacketListener;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.utils.Scheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.*;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DiffuserWebSocketClient extends WebSocketClient {

    private static final int RETRY_INTERVAL = 2500;

    @Getter
    private final static HashMap<String, ServerPacketListener> listeners = new HashMap<>();

    @Getter
    private boolean connected = false;

    private boolean end = false;

    public DiffuserWebSocketClient(URI serverUri) {
        super(serverUri);
        BoostedAudioAPI.api.info("DiffuserWebSocketClient init...");
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            setSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        BoostedAudioAPI.api.info("Diffuser connected to the bungee WebSocket, still not auth!");
        Scheduler.runTaskAsync(() -> {
            String serverName = BoostedAudioSpigot.getInstance().getBungeeServerName();
            if (serverName == null) {
                while (Bukkit.getOnlinePlayers().isEmpty()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                BoostedAudioAPI.getAPI().debug("Sending server name request");
                super.send(BoostedAudioAPI.getAPI().getConfiguration().getBungeeSecrets().get(0) + ";?");
                while (serverName == null && !end) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    serverName = BoostedAudioSpigot.getInstance().getBungeeServerName();
                }
            }
            super.send(BoostedAudioAPI.getAPI().getConfiguration().getBungeeSecrets().get(0) + ";" + serverName);
            BoostedAudioAPI.getAPI().debug("Sending bungee token verif, should be auth!");
            connected = true;
        });
    }

    @Override
    public void onMessage(String message) {
        String[] split = message.split(";", 2);
        ServerPacketListener listener = listeners.get(split[0]);
        if (listener == null) {
            BoostedAudioAPI.getAPI().info("Is plugin updated ? Received a message from the server, but there was no listener for it. Message: " + message);
            return;
        }
        listener.onReceive(split[1], null);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        connected = false;
        BoostedAudioAPI.api.debug("Diffuser connection closed");
    }

    @Override
    public void onError(Exception e) {
        if (BoostedAudioAPI.api.getConfiguration().isDebugMode()) e.printStackTrace();
    }

    @Override
    public void send(String text) {
        if (connected)
            super.send(text);
    }

    public static void registerListener(String channel, ServerPacketListener listener) {
        listeners.put(channel, listener);
    }

    public void end() {
        end = true;
        close();
    }

}