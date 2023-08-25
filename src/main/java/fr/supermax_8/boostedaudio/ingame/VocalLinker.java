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
import java.util.stream.Collectors;

public class VocalLinker extends BukkitRunnable {

    private static final ConnectionManager manager = BoostedAudio.getInstance().getWebSocketServer().manager;
    private static final double maxDistance = BoostedAudio.getInstance().getConfiguration().getMaxVoiceDistance();

    private static final Map<UUID, User> users = manager.getUsers();

    @Override
    public void run() {
        try {
            HashSet<PeerConnection> toLink = new HashSet<>();
            HashSet<PeerConnection> toUnLink = new HashSet<>();

            HashSet<User> connectedUsers = getConnectedUserAndClean();
            HashSet<UUID> connectedUsersIds = new HashSet<>(connectedUsers.parallelStream().map(User::getPlayerId).collect(Collectors.toSet()));
            Map<UUID, List<UUID>> peersMap = calculateUsersPeers(connectedUsers, connectedUsersIds);

            for (User user : connectedUsers) {
                List<UUID> peersOfUser = peersMap.get(user.getPlayerId());

                // Player to add
                for (UUID peer : peersOfUser)
                    if (!user.getRemotePeers().contains(peer))
                        toLink.add(new PeerConnection(user.getPlayerId(), users.get(peer).getPlayerId()));

                // Player to remove
                for (UUID peer : user.getRemotePeers())
                    if (!peersOfUser.contains(peer))
                        toUnLink.add(new PeerConnection(user.getPlayerId(), users.get(peer).getPlayerId()));
            }

            toLink.forEach(PeerConnection::link);
            toUnLink.forEach(PeerConnection::unLink);

            sendUpdatePositions(connectedUsers, peersMap);


            debugSaMere(
                    "Debug:",
                    "Connected users: " + connectedUsers.size(),
                    "Connected users ids: " + connectedUsersIds.size(),
                    "Peers map: " + peersMap.size(),
                    "To link: " + toLink.size(),
                    "To unlink: " + toUnLink.size()
            );
            debugSaMere("ConnectedMap:");
            connectedUsers.forEach(user -> debugSaMere(Bukkit.getPlayer(user.getPlayerId()).getName() + " : " + user.getRemotePeers().size()));
            debugSaMere("PeersMap:");
            peersMap.forEach((id, peers) -> debugSaMere(Bukkit.getPlayer(id).getName() + " : " + peers));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void debugSaMere(String... messages) {
        for (String message : messages) System.out.println(message);
    }

    private void sendUpdatePositions(Set<User> connectedUsers, Map<UUID, List<UUID>> peersMap) {
        for (User user : connectedUsers) {
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


    private HashSet<User> getConnectedUserAndClean() {
        HashSet<User> userList = new HashSet<>();
        for (User user : users.values()) {
            Player player = Bukkit.getPlayer(user.getPlayerId());
            if (player == null) {
                user.getSession().close();
                continue;
            }
            userList.add(user);
        }
        return userList;
    }

    private Map<UUID, List<UUID>> calculateUsersPeers(Set<User> connectedUser, Set<UUID> connectedIds) throws ExecutionException, InterruptedException {
        return Bukkit.getScheduler().callSyncMethod(BoostedAudio.getInstance(), () -> {
            // UUID OF A USER, LIST OF PEERS OF THE USER
            HashMap<UUID, List<UUID>> peerMap = new HashMap<>();
            for (User user : connectedUser) {
                Player player = Bukkit.getPlayer(user.getPlayerId());
                LinkedList<UUID> peers = new LinkedList<>();
                for (Entity entity : player.getNearbyEntities(maxDistance, maxDistance, maxDistance)) {
                    if (entity.getType() != EntityType.PLAYER) continue;
                    UUID id = entity.getUniqueId();
                    if (connectedIds.contains(id)) peers.add(id);
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

            usr1.getRemotePeers().remove(id2);
            usr2.getRemotePeers().remove(id1);
            usr1.sendPacket(new PacketList(new RemovePeerPacket(id2)));
            usr2.sendPacket(new PacketList(new RemovePeerPacket(id1)));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PeerConnection that = (PeerConnection) o;
            return Objects.equals(id1, that.id1) && Objects.equals(id2, that.id2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id1, id2);
        }

    }


}