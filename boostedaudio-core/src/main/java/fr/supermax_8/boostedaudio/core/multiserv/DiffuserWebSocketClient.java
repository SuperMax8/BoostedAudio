package fr.supermax_8.boostedaudio.core.multiserv;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import lombok.Getter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class DiffuserWebSocketClient extends WebSocketClient {

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
        CompletableFuture.runAsync(() -> {
            String serverName = BoostedAudioAPI.getAPI().getConfiguration().getProxyServerName();
            super.send(BoostedAudioAPI.getAPI().getConfiguration().getSecrets().get(0) + ";" + serverName);
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
/*        if (BoostedAudioAPI.getAPI().getConfiguration().isDebugMode()) {
            BoostedAudioAPI.getAPI().debug("Sending packet -> Connected: " + connected + " Packet: " + text);
        }*/
    }

    public static void registerListener(String channel, ServerPacketListener listener) {
        listeners.put(channel, listener);
    }

    public void end() {
        end = true;
        close();
    }

}