package fr.supermax_8.boostedaudio.core.websocket;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.core.BoostedAudioHost;
import org.java_websocket.WebSocket;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private int TOKEN_LENGTH = 3;

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

    protected final ConcurrentHashMap<WebSocket, Optional<HostUser>> sessionUsers = new ConcurrentHashMap<>();

    public ConnectionManager() {
    }


    public ConcurrentHashMap<UUID, String> getPlayerTokens() {
        return playerTokens;
    }

    public ConcurrentHashMap<UUID, User> getUsers() {
        return users;
    }

    public ConcurrentHashMap<WebSocket, Optional<HostUser>> getSessionUsers() {
        return sessionUsers;
    }


    public String generateConnectionToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        random.nextBytes(tokenBytes);

        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        if (playerTokens.containsValue(token)) {
            TOKEN_LENGTH++;
            return generateConnectionToken();
        }
        return token;
    }

    public String generateConnectionToken(UUID playerId) {
        ConnectionManager manager = BoostedAudioHost.getInstance().getWebSocketServer().manager;
        Map<UUID, String> tokenMap = manager.getPlayerTokens();
        if (tokenMap.containsKey(playerId)) {
            User user = manager.getUsers().get(playerId);
            if (user != null) {
                user.close();
                BoostedAudioAPI.api.debug("sendConnectMessage close() session");
            }
        }
        String token = BoostedAudioHost.getInstance().getWebSocketServer().manager.generateConnectionToken();
        tokenMap.put(playerId, token);
        return token;
    }


}