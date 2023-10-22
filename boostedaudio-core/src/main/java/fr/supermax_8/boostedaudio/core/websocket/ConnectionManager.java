package fr.supermax_8.boostedaudio.core.websocket;

import org.java_websocket.WebSocket;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {


    /**
     * UUID: Minecraft player uuid
     * String: token
     */
    protected final ConcurrentHashMap<UUID, String> playerTokens = new ConcurrentHashMap<>();

    /**
     * The users trusted with the token link to the minecraft client
     * String: The token gave by the server in game
     * Session: The websocket session
     */
    protected final ConcurrentHashMap<UUID, User> users = new ConcurrentHashMap<>();

    protected final ConcurrentHashMap<WebSocket, Optional<User>> sessionUsers = new ConcurrentHashMap<>();

    public ConnectionManager() {
    }


    public ConcurrentHashMap<UUID, String> getPlayerTokens() {
        return playerTokens;
    }

    public ConcurrentHashMap<UUID, User> getUsers() {
        return users;
    }

    public ConcurrentHashMap<WebSocket, Optional<User>> getSessionUsers() {
        return sessionUsers;
    }

}