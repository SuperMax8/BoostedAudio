package fr.supermax_8.boostedaudio.web.packets;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.BoostedAudioConfiguration;
import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.Packet;
import fr.supermax_8.boostedaudio.web.User;

import java.util.Map;
import java.util.UUID;

public class TrustPacket implements Packet {

    private final String token;
    private final ServerInfo serverInfo;

    public TrustPacket(String token, ServerInfo serverInfo) {
        this.token = token;
        this.serverInfo = serverInfo;
    }

    @Override
    public void onReceive(User user, AudioWebSocketServer server) {
        UUID playerId;

        if ((playerId = getKeyByValue(server.manager.getPlayerTokens(), token)) != null) {
            User newUser = new User(user.getSession(), token, playerId);
            server.manager.getUsers().put(playerId, newUser);
            server.manager.getSessionUsers().put(user.getSession(), newUser);
            BoostedAudio.debug("New trusted: " + playerId);
            BoostedAudioConfiguration configuration = BoostedAudio.getInstance().getConfiguration();
            newUser.send(new TrustPacket(null, new ServerInfo(
                    configuration.getMaxVoiceDistance(), configuration.getRolloffFactor(), configuration.getRefDistance(), configuration.getDistanceModel())
            ));
        } else user.getSession().close();
    }


    public static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) if (entry.getValue().equals(value)) return entry.getKey();
        return null;
    }

    public static class ServerInfo {

        private final double maxDistance;
        private final float rolloffFactor;
        private final float refDistance;
        private final String distanceModel;

        public ServerInfo(double maxDistance, float rolloffFactor, float refDistance, String distanceModel) {
            this.maxDistance = maxDistance;
            this.rolloffFactor = rolloffFactor;
            this.refDistance = refDistance;
            this.distanceModel = distanceModel;
        }

    }

}