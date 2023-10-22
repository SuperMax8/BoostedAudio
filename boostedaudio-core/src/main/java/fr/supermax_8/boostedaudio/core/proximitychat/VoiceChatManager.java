package fr.supermax_8.boostedaudio.core.proximitychat;

import fr.supermax_8.boostedaudio.core.utils.SerializableLocation;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.User;
import fr.supermax_8.boostedaudio.core.websocket.packets.UpdateVocalPositionsPacket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// Host Only
public class VoiceChatManager {


    public VoiceChatManager() {

    }


    public void processResult(VoiceChatResult result) {
        for (LayerInfo layerInfo : result.getLayers()) processLayer(layerInfo);
    }

    private void processLayer(LayerInfo layerInfo) {
        processPeersUpdate(layerInfo);
        processPositionUpdate(layerInfo);
    }

    private void processPeersUpdate(LayerInfo layerInfo) {
        HashSet<PeerConnection> toLink = new HashSet<>();
        HashSet<PeerConnection> toUnLink = new HashSet<>();

        fillLinkUnlink(toLink, toUnLink, layerInfo);
        System.out.println("toLink = " + toLink);
        System.out.println("toUnLink = " + toUnLink);

        toLink.forEach(PeerConnection::link);
        toUnLink.forEach(PeerConnection::unLink);
    }

    private void fillLinkUnlink(Set<PeerConnection> toLink, Set<PeerConnection> toUnLink, LayerInfo layerInfo) {
        ConcurrentHashMap<UUID, User> users = AudioWebSocketServer.getInstance().manager.getUsers();

        for (Map.Entry<UUID, PlayerInfo> entry : layerInfo.getPlayersInfo().entrySet()) {
            User user = users.get(entry.getKey());
            PlayerInfo playerInfo = entry.getValue();

            List<UUID> newPeersOfUser = playerInfo.getPeers();
            Set<UUID> oldPeersOfUser = user.getRemotePeers(layerInfo.getLayerId());

            System.out.println("USERRRR" + user.getPlayerId());
            System.out.println("newPeersOfUser=" + newPeersOfUser);
            System.out.println("oldPeersOfUser=" + oldPeersOfUser);

            if (newPeersOfUser == null || oldPeersOfUser == null) continue;

            // Player to add
            System.out.println("TOLINK");
            processPeers(users, user, layerInfo.getLayerId(), toLink, newPeersOfUser, oldPeersOfUser);
            // Player to remove
            System.out.println("TOUNLINK");
            processPeers(users, user, layerInfo.getLayerId(), toUnLink, oldPeersOfUser, newPeersOfUser);
        }
    }

    private void processPeers(ConcurrentHashMap<UUID, User> users, User user, String layerId, Set<PeerConnection> links, Collection<UUID> peers1, Collection<UUID> peers2) {
        for (UUID peer : peers1) {
            if (!peers2.contains(peer)) {
                User peerUsr = users.get(peer);
                if (peerUsr != null) {
                    System.out.println("LINKKK" + peerUsr.getPlayerId());
                    links.add(new PeerConnection(user.getPlayerId(), peerUsr.getPlayerId(), layerId));
                }
            }
        }
    }

    private void processPositionUpdate(LayerInfo layerInfo) {
        for (Map.Entry<UUID, PlayerInfo> entry : new HashSet<>(layerInfo.getPlayersInfo().entrySet())) {
            UUID playerId = entry.getKey();
            PlayerInfo playerInfo = entry.getValue();
            ConcurrentHashMap<UUID, User> users = AudioWebSocketServer.getInstance().manager.getUsers();
            User user = users.get(playerId);

            sendUpdatePositions(user, playerInfo.getLocation(), playerInfo.getPeers().stream().collect(Collectors.toMap(
                    peerId -> peerId,
                    peerId -> layerInfo.getPlayersInfo().get(peerId).getLocation()
            )));
        }
    }

    private void sendUpdatePositions(User user, SerializableLocation playerLocation, Map<UUID, SerializableLocation> peerLocations) {
        UpdateVocalPositionsPacket updatePacket = new UpdateVocalPositionsPacket(
                playerLocation,
                peerLocations
        );
        user.sendPacket(updatePacket);
    }


}