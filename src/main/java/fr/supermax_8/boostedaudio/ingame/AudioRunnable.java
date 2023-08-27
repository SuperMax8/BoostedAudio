package fr.supermax_8.boostedaudio.ingame;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.utils.SerializableLocation;
import fr.supermax_8.boostedaudio.web.ConnectionManager;
import fr.supermax_8.boostedaudio.web.PacketList;
import fr.supermax_8.boostedaudio.web.User;
import fr.supermax_8.boostedaudio.web.packets.AddPeerPacket;
import fr.supermax_8.boostedaudio.web.packets.RemovePeerPacket;
import fr.supermax_8.boostedaudio.web.packets.UpdateVocalPositionsPacket;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class AudioRunnable extends BukkitRunnable {

    private static final ConnectionManager manager = BoostedAudio.getInstance().getWebSocketServer().manager;
    private static final double maxDistance = BoostedAudio.getInstance().getConfiguration().getMaxVoiceDistance();
    private static final Map<UUID, User> users = manager.getUsers();

    private static RegionManager regionManager = RegionManager.create();

    @Override
    public void run() {
        try {
            HashMap<UUID, User> connectedUsers = getConnectedUserAndClean();
            Map<UUID, List<UUID>> peersMap = calculateUsersPeers(connectedUsers);
            HashSet<PeerConnection> toLink = new HashSet<>();
            HashSet<PeerConnection> toUnLink = new HashSet<>();

            fillLinkUnlink(toLink, toUnLink, connectedUsers, peersMap);

            toLink.forEach(PeerConnection::link);
            toUnLink.forEach(PeerConnection::unLink);

            sendUpdatePositions(connectedUsers, peersMap);

            if (regionManager != null) regionManager.tick(connectedUsers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void vocalLinker() {

    }

    private void fillLinkUnlink(Set<PeerConnection> toLink, Set<PeerConnection> toUnLink, Map<UUID, User> connectedUsers, Map<UUID, List<UUID>> peersMap) {
        for (User user : connectedUsers.values()) {
            List<UUID> currentPeersOfUser = peersMap.get(user.getPlayerId());
            Set<UUID> oldPeersOfUser = user.getRemotePeers();

            // Player to add
            for (UUID peer : currentPeersOfUser) {
                if (!oldPeersOfUser.contains(peer))
                    toLink.add(new PeerConnection(user.getPlayerId(), connectedUsers.get(peer).getPlayerId()));
            }

            // Player to remove
            for (UUID peer : user.getRemotePeers()) {
                if (!currentPeersOfUser.contains(peer)) {
                    toUnLink.add(new PeerConnection(user.getPlayerId(), connectedUsers.get(peer).getPlayerId()));
                }
            }
        }
    }

    private void sendUpdatePositions(Map<UUID, User> connectedUsers, Map<UUID, List<UUID>> peersMap) {
        for (User user : connectedUsers.values()) {
            List<UUID> peers = peersMap.get(user.getPlayerId());

            HashMap<UUID, SerializableLocation> peersLocs = new HashMap<>();
            Location playerLoc = Bukkit.getPlayer(user.getPlayerId()).getLocation();
            for (UUID id : peers) peersLocs.put(id, new SerializableLocation(Bukkit.getPlayer(id).getLocation()));

            UpdateVocalPositionsPacket updatePacket = new UpdateVocalPositionsPacket(
                    new SerializableLocation(playerLoc),
                    peersLocs
            );
            user.sendPacket(updatePacket);
        }
    }


    private HashMap<UUID, User> getConnectedUserAndClean() {
        HashMap<UUID, User> userList = new HashMap<>();
        for (User user : users.values()) {
            Player player = Bukkit.getPlayer(user.getPlayerId());
            if (player == null) {
                user.getSession().close();
                continue;
            }
            userList.put(user.getPlayerId(), user);
        }
        return userList;
    }

    private Map<UUID, List<UUID>> calculateUsersPeers(HashMap<UUID, User> connectedUser) throws ExecutionException, InterruptedException {
        return Bukkit.getScheduler().callSyncMethod(BoostedAudio.getInstance(), () -> {
            // UUID OF A USER, LIST OF PEERS OF THE USER
            HashMap<UUID, List<UUID>> peerMap = new HashMap<>();
            for (User user : connectedUser.values()) {
                Player player = Bukkit.getPlayer(user.getPlayerId());
                LinkedList<UUID> peers = new LinkedList<>();
                for (Entity entity : player.getNearbyEntities(maxDistance, maxDistance, maxDistance)) {
                    if (entity.getType() != EntityType.PLAYER) continue;
                    UUID id = entity.getUniqueId();
                    if (connectedUser.containsKey(id)) peers.add(id);
                }
                peerMap.put(user.getPlayerId(), peers);
            }
            return peerMap;
        }).get();
    }


    public static class PeerConnection {

        private final UUID id1;
        private final UUID id2;

        public PeerConnection(UUID id1, UUID id2) {
            this.id1 = id1;
            this.id2 = id2;
        }

        public void link() {
            User usr1 = users.get(id1);
            User usr2 = users.get(id2);
            usr1.getRemotePeers().add(id2);
            usr2.getRemotePeers().add(id1);

            AddPeerPacket peerPacket = new AddPeerPacket(new AddPeerPacket.RTCDescription("", "createoffer"), usr1.getPlayerId(), usr2.getPlayerId());

            usr2.sendPacket(peerPacket);
        }

        public void unLink() {
            User usr1 = users.get(id1);
            User usr2 = users.get(id2);

            if (usr1 != null) {
                usr1.getRemotePeers().remove(id2);
                usr1.sendPacket(new PacketList(new RemovePeerPacket(id2)));
            }
            if (usr2 != null) {
                usr2.getRemotePeers().remove(id1);
                usr2.sendPacket(new PacketList(new RemovePeerPacket(id1)));
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PeerConnection that = (PeerConnection) o;
            return
                    (id1.equals(that.id1) && id2.equals(that.id2))
                            ||
                            (id1.equals(that.id2) && id2.equals(that.id1));
        }

        @Override
        public int hashCode() {
            String str1 = id1.toString();
            String str2 = id2.toString();
            String concatenated = str1.compareTo(str2) < 0 ? str1 + str2 : str2 + str1;

            return concatenated.hashCode();
        }

    }


}