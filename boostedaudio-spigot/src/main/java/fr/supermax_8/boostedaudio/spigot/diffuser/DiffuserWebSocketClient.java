package fr.supermax_8.boostedaudio.spigot.diffuser;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.serverpacket.ServerPacketListener;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.utils.Scheduler;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.*;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class DiffuserWebSocketClient extends WebSocketClient {

    private final HashMap<String, ServerPacketListener> listeners = new HashMap<>();

    private boolean connected = false;

    private boolean closed = false;

    public DiffuserWebSocketClient(URI serverUri) {
        super(serverUri);
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
        BoostedAudioAPI.api.info("Diffuser connected to the bungee WebSocket!");
        Scheduler.runTaskLaterAsync(() -> {
            String serverName = BoostedAudioSpigot.getInstance().getBungeeServerName();
            if (serverName == null) super.send(BoostedAudioAPI.getAPI().getConfiguration().getBungeeSecrets().get(0) + ";?");
            while (serverName == null && !closed) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                serverName = BoostedAudioSpigot.getInstance().getBungeeServerName();
            }
            if (closed) return;
            super.send(BoostedAudioAPI.getAPI().getConfiguration().getBungeeSecrets().get(0) + ";" + serverName);
            BoostedAudioAPI.getAPI().debug("Sending bungee token verif !");
            connected = true;
        }, 20);
    }

    @Override
    public void onMessage(String message) {
        String[] split = message.split(";", 2);
        ServerPacketListener listener = listeners.get(split[0]);
        if (listener == null) {
            BoostedAudioAPI.getAPI().info("Plugin is update ? Received a message from the server, but there was no listener for it. Message: " + message);
            return;
        }
        listener.onReceive(split[1], null);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        if (closed) return;
        BoostedAudioAPI.api.debug("Connection closed, Attempting to reconnect in 5s...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            CompletableFuture.runAsync(this::reconnect);
        } catch (Exception e) {
            if (BoostedAudioAPI.api.getConfiguration().isDebugMode()) e.printStackTrace();
        }
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

    public HashMap<String, ServerPacketListener> getListeners() {
        return listeners;
    }

    public void registerListener(String channel, ServerPacketListener listener) {
        listeners.put(channel, listener);
    }

    @Override
    public void close() {
        closed = true;
        super.close();
    }

}