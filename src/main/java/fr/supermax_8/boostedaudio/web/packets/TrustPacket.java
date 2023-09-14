package fr.supermax_8.boostedaudio.web.packets;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.BoostedAudioConfiguration;
import fr.supermax_8.boostedaudio.web.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class TrustPacket implements Packet {

    private final String token;
    private final ServerInfo serverInfo;

    public TrustPacket(String token, ServerInfo serverInfo) {
        this.token = token;
        this.serverInfo = serverInfo;
    }

    @Override
    public void onReceive(User user, AudioWebSocket server) {
        UUID playerId;
        ConnectionManager manager = server.manager;
        if ((playerId = getKeyByValue(manager.getPlayerTokens(), token)) != null) {
            User newUser = new User(user.getSession(), token, playerId);
            if (!BoostedAudio.getInstance().isPremium()) {
                if (manager.getUsers().size() >= FreeVersionLimit.getMaxUserConnected()) {
                    BoostedAudio.info("You have reached the maximum number of connected users for the free version, for no limit, please consider buying the plugin");
                    try {
                        user.getSession().close();
                    } catch (Exception e) {
                    }
                    return;
                }
            }

            manager.getUsers().put(playerId, newUser);
            manager.getSessionUsers().put(user.getSession(), Optional.of(newUser));
            BoostedAudio.debug("New trusted: " + playerId);
            BoostedAudioConfiguration configuration = BoostedAudio.getInstance().getConfiguration();
            newUser.sendPacket(new TrustPacket(null, new ServerInfo(
                    configuration.getMaxVoiceDistance(), configuration.getRolloffFactor(), configuration.getRefDistance(), configuration.getDistanceModel())
            ));
            BoostedAudio.getInstance().getAudioManager().getRegionManager().getInfoMap().get(playerId).setLastRegions(new CopyOnWriteArrayList<>());
        } else {
            try {
                user.getSession().close();
            } catch (Exception e) {
            }
            BoostedAudio.debug("onReceive close() session");
        }
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