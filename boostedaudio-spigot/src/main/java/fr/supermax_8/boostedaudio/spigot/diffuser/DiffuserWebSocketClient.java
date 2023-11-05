package fr.supermax_8.boostedaudio.spigot.diffuser;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.serverpacket.ServerPacketListener;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.security.AlgorithmConstraints;
import java.security.AlgorithmParameterGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class DiffuserWebSocketClient extends WebSocketClient {

    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private int currentReconnectAttempts = 0;

    private final HashMap<String, ServerPacketListener> listeners = new HashMap<>();

    public DiffuserWebSocketClient(URI serverUri) {
        super(serverUri);
        /*setSocketFactory(SSLSocketFactory.getDefault());*/
        try {
// load up the key store
/*            String STORETYPE = "JKS";
            String KEYSTORE = Paths.get("src", "test", "java", "org", "java_websocket", "keystore.jks")
                    .toString();
            String STOREPASSWORD = "storepassword";
            String KEYPASSWORD = "keypassword";*/

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
        System.out.println("WebSocket Init...");
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Connected to the WebSocket!");
        currentReconnectAttempts = 0;
        send(BoostedAudioAPI.getAPI().getConfiguration().getBungeeSecrets().get(0));
    }

    @Override
    public void onMessage(String message) {
        String[] split = message.split(";", 2);
        ServerPacketListener listener = listeners.get(split[0]);
        if (listener == null) {
            System.out.println("Plugin is update ? Received a message from the server, but there was no listener for it. Message: " + message);
            return;
        }
        listener.onResponse(split[1], null);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("Connection closed");
        if (currentReconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            System.out.println("Attempting to reconnect...");
            try {
                currentReconnectAttempts++;
                CompletableFuture.runAsync(this::reconnect);
            } catch (Exception e) {
                if (BoostedAudioAPI.api.getConfiguration().isDebugMode()) e.printStackTrace();
            }
        } else {
            System.out.println("Maximum reconnection attempts reached. Exiting.");
            // Add your desired logic here for handling the maximum reconnection attempts
        }
    }

    @Override
    public void onError(Exception e) {
        if (BoostedAudioAPI.api.getConfiguration().isDebugMode()) e.printStackTrace();
    }

    public HashMap<String, ServerPacketListener> getListeners() {
        return listeners;
    }

    public void registerListener(String channel, ServerPacketListener listener) {
        listeners.put(channel, listener);
    }

}