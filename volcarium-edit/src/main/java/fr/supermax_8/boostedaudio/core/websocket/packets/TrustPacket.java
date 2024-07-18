package fr.supermax_8.boostedaudio.core.websocket.packets;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.event.EventManager;
import fr.supermax_8.boostedaudio.api.event.events.UserJoinEvent;
import fr.supermax_8.boostedaudio.api.packet.Packet;
import fr.supermax_8.boostedaudio.core.BoostedAudioConfiguration;
import fr.supermax_8.boostedaudio.core.Limiter;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.ConnectionManager;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;
import fr.supermax_8.boostedaudio.core.websocket.ServerInfo;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TrustPacket implements Packet {

    private final String token;
    private final ServerInfo serverInfo;

    public TrustPacket(String token, ServerInfo serverInfo) {
        this.token = token;
        this.serverInfo = serverInfo;
    }

    @Override
    public void onReceive(HostUser user, AudioWebSocketServer server) {
        UUID playerId;
        ConnectionManager manager = server.manager;
        if ((playerId = getKeyByValue(manager.getPlayerTokens(), token)) != null) {
            HostUser newUser = new HostUser(user.getSession(), token, playerId);
            if (!Limiter.isPremium()) {
                if (manager.getUsers().size() >= Limiter.getMaxUserConnected()) {
                    BoostedAudioAPI.api.info("You have reached the maximum number of connected users for the free version, for no limit, please consider buying the plugin");
                    user.getSession().close();
                    return;
                }
            }

            manager.getPlayerTokens().remove(playerId);
            newUser.applyMuteIfMute();
            manager.getUsers().put(playerId, newUser);
            manager.getSessionUsers().put(user.getSession(), Optional.of(newUser));
            BoostedAudioAPI.api.debug("New trusted: " + playerId);
            BoostedAudioConfiguration configuration = BoostedAudioAPI.api.getConfiguration();
            newUser.sendPacket(new TrustPacket(null, new ServerInfo(
                    configuration.getMaxVoiceDistance(),
                    configuration.getRolloffFactor(),
                    configuration.getRefDistance(),
                    configuration.getDistanceModel(),
                    playerId.toString()
            )
            ));
            newUser.sendIceServers();
            UserJoinEvent userJoinEvent = new UserJoinEvent(newUser);
            EventManager.getInstance().callEvent(userJoinEvent);
        } else {
            user.getSession().close();
            BoostedAudioAPI.api.debug("onReceive close() session");
        }
    }


    public static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) if (entry.getValue().equals(value)) return entry.getKey();
        return null;
    }

}