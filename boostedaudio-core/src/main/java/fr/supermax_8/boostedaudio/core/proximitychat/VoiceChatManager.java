package fr.supermax_8.boostedaudio.core.proximitychat;

import fr.supermax_8.boostedaudio.api.user.User;
import fr.supermax_8.boostedaudio.core.utils.SerializableLocation;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;
import fr.supermax_8.boostedaudio.core.websocket.packets.UpdatePeersLocationsPacket;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// Host Only
public class VoiceChatManager {

    @Getter
    private static final HashMap<UUID, MuteUser> mutedUsers = new HashMap<>();

    public VoiceChatManager() {

    }

    public void processResult(VoiceChatResult result) {
        Iterator<Map.Entry<UUID, MuteUser>> iterator = mutedUsers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, MuteUser> entry = iterator.next();
            if (!entry.getValue().isMuted()) {
                iterator.remove();
                User user = AudioWebSocketServer.getInstance().manager.getUsers().get(entry.getKey());
                user.setMuted(false, 0);
                user.getRemotePeers().clear();
            }
        }

        for (LayerInfo layerInfo : result.getLayers()) processLayer(layerInfo);
    }

    private void processLayer(LayerInfo layerInfo) {
        checkInfo(layerInfo);
        processPeersUpdate(layerInfo);
        processPositionUpdate(layerInfo);
    }

    private void checkInfo(LayerInfo layerInfo) {
        for (UUID uuid : new LinkedList<>(layerInfo.getPlayersInfo().keySet())) {
            if (!AudioWebSocketServer.getInstance().manager.getUsers().containsKey(uuid))
                layerInfo.getPlayersInfo().remove(uuid);
        }
    }

    private void processPeersUpdate(LayerInfo layerInfo) {
        HashSet<PeerConnection> toLink = new HashSet<>();
        HashSet<PeerConnection> toUnLink = new HashSet<>();

        fillLinkUnlink(toLink, toUnLink, layerInfo);

        toLink.forEach(PeerConnection::link);
        toUnLink.forEach(PeerConnection::unLink);
/*        BoostedAudioAPI.getAPI().info(layerInfo.getLayerId());
        BoostedAudioAPI.getAPI().info("toLink: " + toLink.size());
        BoostedAudioAPI.getAPI().info("toUnLink: " + toUnLink.size());*/
    }

    private void fillLinkUnlink(Set<PeerConnection> toLink, Set<PeerConnection> toUnLink, LayerInfo layerInfo) {
        ConcurrentHashMap<UUID, User> users = AudioWebSocketServer.getInstance().manager.getUsers();

        for (Map.Entry<UUID, PlayerInfo> entry : layerInfo.getPlayersInfo().entrySet()) {
            HostUser user = (HostUser) users.get(entry.getKey());
            PlayerInfo playerInfo = entry.getValue();
            Set<UUID> oldPeersOfUser = user.getRemotePeers(layerInfo.getLayerId());

            if (playerInfo.isMuted()) {
                if (oldPeersOfUser != null) {
                    for (UUID peer : oldPeersOfUser) {
                        User peerUsr = users.get(peer);
                        if (peerUsr != null)
                            toUnLink.add(new PeerConnection(user.getPlayerId(), peerUsr.getPlayerId(), layerInfo.getLayerId()));
                    }
                }
                continue;
            }

            List<UUID> newPeersOfUser = playerInfo.getPeers();

            if (newPeersOfUser == null || oldPeersOfUser == null) continue;

            for (UUID peer : newPeersOfUser) {
                if (!oldPeersOfUser.contains(peer)) {
                    User peerUsr = users.get(peer);
                    if (peerUsr != null)
                        toLink.add(new PeerConnection(user.getPlayerId(), peerUsr.getPlayerId(), layerInfo.getLayerId()));
                }
            }

            for (UUID peer : oldPeersOfUser) {
                if (!newPeersOfUser.contains(peer)) {
                    User peerUsr = users.get(peer);
                    if (peerUsr != null)
                        toUnLink.add(new PeerConnection(user.getPlayerId(), peerUsr.getPlayerId(), layerInfo.getLayerId()));
                }
            }

            // Player to add
            // processPeers(users, user, layerInfo.getLayerId(), toLink, newPeersOfUser, oldPeersOfUser);
            // Player to remove
            // processPeers(users, user, layerInfo.getLayerId(), toUnLink, oldPeersOfUser, newPeersOfUser);
        }
    }

    private void processPeers(ConcurrentHashMap<UUID, User> users, HostUser user, String layerId, Set<PeerConnection> links, Collection<UUID> peers1, Collection<UUID> peers2) {
        for (UUID peer : peers1) {
            if (!peers2.contains(peer)) {
                User peerUsr = users.get(peer);
                if (peerUsr != null)
                    links.add(new PeerConnection(user.getPlayerId(), peerUsr.getPlayerId(), layerId));
            }
        }
    }

    private void processPositionUpdate(LayerInfo layerInfo) {
        Map<UUID, PlayerInfo> playerInfoMap = layerInfo.getPlayersInfo();
        for (Map.Entry<UUID, PlayerInfo> entry : new HashSet<>(playerInfoMap.entrySet())) {
            UUID playerId = entry.getKey();
            PlayerInfo playerInfo = entry.getValue();
            ConcurrentHashMap<UUID, User> users = AudioWebSocketServer.getInstance().manager.getUsers();
            User user = users.get(playerId);

            Map<UUID, SerializableLocation> peerLocations = new HashMap<>();
            for (UUID id : playerInfo.getPeers()) {
                PlayerInfo info = playerInfoMap.get(id);
                if (info != null)
                    peerLocations.put(id, info.getLocation());
            }

            sendUpdatePositions(user, playerInfo.getLocation(), peerLocations);
        }
    }

    private void sendUpdatePositions(User user, SerializableLocation playerLocation, Map<UUID, SerializableLocation> peerLocations) {
        UpdatePeersLocationsPacket updatePacket = new UpdatePeersLocationsPacket(
                playerLocation,
                peerLocations
        );
        user.sendPacket(updatePacket);
        //System.out.println("UpdatePeersLocationsPacket for user " + user);
    }


    public static class MuteUser {

        private final long muteEnd;
        @Getter
        private UUID playerId;

        public MuteUser(UUID playerId, long muteEnd) {
            this.playerId = playerId;
            this.muteEnd = muteEnd;
        }

        public boolean isMuted() {
            return System.currentTimeMillis() < muteEnd;
        }

    }

}