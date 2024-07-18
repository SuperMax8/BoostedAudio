package fr.supermax_8.boostedaudio.core.websocket;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.core.BoostedAudioConfiguration;
import fr.supermax_8.boostedaudio.core.BoostedAudioHost;
import fr.supermax_8.boostedaudio.core.multiserv.ServerUser;
import fr.supermax_8.boostedaudio.core.utils.Base64Utils;
import lombok.Getter;
import org.java_websocket.WebSocket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public class ConnectionManager {

    @Getter
    private static ConnectionManager instance;

    public static int TOKEN_LENGTH = 3;

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

    protected final ConcurrentHashMap<WebSocket, Optional<Object>> sessionUsers = new ConcurrentHashMap<>();

    protected final ConcurrentHashMap<UUID, ServerUser> servers = new ConcurrentHashMap<>();

    protected ScheduledExecutorService schedule;

    public ConnectionManager() {
        instance = this;
        schedule = Executors.newSingleThreadScheduledExecutor();
        schedule.scheduleAtFixedRate(() -> {
            for (User user : users.values()) {
                HostUser hostUser = (HostUser) user;
                List<TurnCredential> credentials = hostUser.getTurnCredentials();
                if (credentials == null) continue;
                for (TurnCredential credential : credentials) {
                    long timeBeforeExpiration = credential.getExpirationTsMs() - System.currentTimeMillis();
                    if (timeBeforeExpiration < 0 || timeBeforeExpiration < TimeUnit.SECONDS.toMillis(20)) {
                        hostUser.sendIceServers();
                        break;
                    }
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public JsonArray generateIceServers(HostUser user) {
        BoostedAudioConfiguration configuration = BoostedAudioAPI.getAPI().getConfiguration();

        JsonArray iceServersJson = configuration.getIceServersJson();
        List<TurnConfig> turnConfigs = configuration.getTurnConfigs();
        if (turnConfigs == null) return iceServersJson;

        JsonArray servers = new JsonArray();
        servers.addAll(iceServersJson);

        List<TurnCredential> credentials = new ArrayList<>();
        for (TurnConfig turnConfig : turnConfigs) {
            TurnCredential credential = turnConfig.generateTurnCredential();
            JsonObject server = new JsonObject();
            server.addProperty("urls", turnConfig.getUrl());
            server.addProperty("username", credential.getUsername());
            server.addProperty("credential", credential.getCredential());
            servers.add(server);
            credentials.add(credential);
        }
        user.setTurnCredentials(credentials);
        return servers;
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