package fr.supermax_8.boostedaudio.core.multiserv;

import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.UUID;

public class ServerUser {

    private final String secret;
    private final String name;
    private final UUID serverId;
    private final WebSocket webSocket;
    private final HashMap<String, ServerPacketListener> listeners = new HashMap<>();

    public ServerUser(String secret, String name, UUID serverId, WebSocket webSocket) {
        this.secret = secret;
        this.name = name;
        this.serverId = serverId;
        this.webSocket = webSocket;
    }


    public String getSecret() {
        return secret;
    }

    public String getName() {
        return name;
    }

    public UUID getServerId() {
        return serverId;
    }

    public HashMap<String, ServerPacketListener> getListeners() {
        return listeners;
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

}