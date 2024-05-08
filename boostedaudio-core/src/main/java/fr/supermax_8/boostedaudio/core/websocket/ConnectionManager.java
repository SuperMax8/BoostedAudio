package fr.supermax_8.boostedaudio.core.websocket;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.core.BoostedAudioHost;
import fr.supermax_8.boostedaudio.core.multiserv.ServerUser;
import fr.supermax_8.boostedaudio.core.utils.Base64Utils;
import lombok.Getter;
import org.java_websocket.WebSocket;

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
    @Getter
    protected final ConcurrentHashMap<UUID, String> playerTokens = new ConcurrentHashMap<>();

    /**
     * The users trusted with the token link to the minecraft client
     * String: The token gave by the server in game
     * Session: The websocket session
     */
    @Getter
    protected final ConcurrentHashMap<UUID, User> users = new ConcurrentHashMap<>();

    @Getter
    protected final ConcurrentHashMap<WebSocket, Optional<Object>> sessionUsers = new ConcurrentHashMap<>();

    @Getter
    protected final ConcurrentHashMap<UUID, ServerUser> servers = new ConcurrentHashMap<>();

    public ConnectionManager() {
    }


    public ServerUser getServer(String serverName) {
        for (ServerUser user : servers.values()) {
            if (user.getName().equals(serverName)) return user;
        }
        return null;
    }

    public String generateConnectionToken() {
        String token = Base64Utils.generateSecuredToken(TOKEN_LENGTH);
        if (playerTokens.containsValue(token)) {
            TOKEN_LENGTH++;
            return generateConnectionToken();
        }
        return token;
    }

    public String generateConnectionToken(UUID playerId) {
        Map<UUID, String> tokenMap = getPlayerTokens();
        if (tokenMap.containsKey(playerId)) {
            User user = getUsers().get(playerId);
            if (user != null) {
                user.close();
                BoostedAudioAPI.api.debug("sendConnectMessage close() session");
            }
        }
        String token = BoostedAudioHost.getInstance().getWebSocketServer().manager.generateConnectionToken();
        tokenMap.put(playerId, token);
        return token;
    }

    public String getConnectionToken(UUID playerId) {
        if (playerTokens.containsKey(playerId)) return playerTokens.get(playerId);
        else return generateConnectionToken(playerId);
    }


}