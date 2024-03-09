package fr.supermax_8.boostedaudio.spigot.proximitychat;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.core.proximitychat.LayerInfo;
import fr.supermax_8.boostedaudio.core.proximitychat.PlayerInfo;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceChatResult;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceLayer;
import fr.supermax_8.boostedaudio.core.utils.SerializableLocation;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.utils.InternalUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
public class VoiceChatProcessor {

    // LayerId : Layer
    private final ConcurrentHashMap<String, VoiceLayer> layers = new ConcurrentHashMap<>();

    private static final double maxDistance = BoostedAudioAPI.api.getConfiguration().getMaxVoiceDistance();

    public VoiceChatProcessor() {
    }

    public void process(Consumer<VoiceChatResult> afterMath) {
        Map<UUID, User> userOnServer = BoostedAudioAPI.api.getHostProvider().getUsersOnServer();
        if (userOnServer == null) return;

        try {
            if (BoostedAudioAPI.getAPI().getConfiguration().isVoiceChatEnabled())
                calculateUsersPeers(new HashMap<>(userOnServer), peers -> {
                    List<LayerInfo> layerInfos = new ArrayList<>();
                    // Process every layers
                    for (Map.Entry<String, VoiceLayer> entry : layers.entrySet()) {
                        VoiceLayer layer = entry.getValue();
                        layerInfos.add(processLayer(layer, userOnServer, peers));
                    }

                    VoiceChatResult result = new VoiceChatResult(layerInfos);
                    //System.out.println("Result: " + result);
                    afterMath.accept(result);
                });
            else {
                // VoiceChat false support
                HashMap<UUID, PlayerInfo> players = new HashMap<>();
                userOnServer.values().forEach(usr -> {
                    Player p = Bukkit.getPlayer(usr.getPlayerId());
                    players.put(p.getUniqueId(), new PlayerInfo(InternalUtils.bukkitLocationToSerializableLoc(p.getLocation()), true));
                });

                LayerInfo layerInfo = new LayerInfo(players, "clientLocs", false);
                VoiceChatResult result = new VoiceChatResult(List.of(layerInfo));
                afterMath.accept(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LayerInfo processLayer(VoiceLayer layer, Map<UUID, User> userOnServer, Map<UUID, List<UUID>> peers) {
        // Layer cached
        Predicate<UUID> predicate = layer.getPlayerInLayer();
        Set<UUID> playersInside = layer.getPlayersInside();

        // Result of the layer
        Map<UUID, PlayerInfo> result = new HashMap<>();

        // Check every user on server
        for (User user : userOnServer.values()) {
            UUID userId = user.getPlayerId();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(userId);
            // Kick from panel if offline
            if (!offlinePlayer.isOnline()) continue;

            // Check if player is inside the layer
            if (predicate != null) {
                if (predicate.test(user.getPlayerId())) playersInside.add(userId);
                else playersInside.remove(userId);
            } else if (layer.isAudioSpatialized()) playersInside.add(userId);


            SerializableLocation pLoc = InternalUtils.bukkitLocationToSerializableLoc(offlinePlayer.getPlayer().getLocation());
            // Put result
            if (layer.isAudioSpatialized()) {
                List<UUID> globalPeersOfUser = peers.get(userId);
                if (globalPeersOfUser == null) continue;
                PlayerInfo playerInfo = new PlayerInfo(pLoc, user.isMuted());
                result.put(userId, playerInfo);
                for (UUID peer : globalPeersOfUser) {
                    if (playersInside.contains(peer))
                        playerInfo.getPeers().add(peer);
                }
            } else {
                Set<UUID> peersOfPlayer = new HashSet<>(playersInside);
                peersOfPlayer.remove(userId);
                result.put(userId, new PlayerInfo(peersOfPlayer, pLoc, user.isMuted()));
            }
        }

        return new LayerInfo(result, layer.getId(), layer.isAudioSpatialized());
    }

    private void calculateUsersPeers(Map<UUID, User> connectedUser, Consumer<Map<UUID, List<UUID>>> afterMath) {
        if (BoostedAudioSpigot.getInstance().getFolia().isFolia())
            CompletableFuture.runAsync(() -> afterMath.accept(getPeerMapFolia(connectedUser)));
        else
            Bukkit.getScheduler().runTask(BoostedAudioSpigot.getInstance(), () -> afterMath.accept(getPeerMap(connectedUser)));
    }

    private HashMap<UUID, List<UUID>> getPeerMap(Map<UUID, User> connectedUser) {
        // UUID OF A USER, LIST OF PEERS OF THE USER
        HashMap<UUID, List<UUID>> peerMap = new HashMap<>();

        for (User user : connectedUser.values()) {
            Player player = Bukkit.getPlayer(user.getPlayerId());
            if (player != null) getPeerPlayer(player, connectedUser, user, peerMap);
        }
        return peerMap;
    }

    private HashMap<UUID, List<UUID>> getPeerMapFolia(Map<UUID, User> connectedUser) {
        // UUID OF A USER, LIST OF PEERS OF THE USER
        HashMap<UUID, List<UUID>> peerMap = new HashMap<>();
        CountDownLatch cd = new CountDownLatch(connectedUser.size());

        for (User user : connectedUser.values()) {
            Player player = Bukkit.getPlayer(user.getPlayerId());
            if (player == null) {
                cd.countDown();
                continue;
            }
            BoostedAudioSpigot.getInstance().getScheduler().runAtEntity(player, task -> {
                getPeerPlayer(player, connectedUser, user, peerMap);
                cd.countDown();
            });
        }

        try {
            cd.await(50, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return peerMap;
    }

    private void getPeerPlayer(Player player, Map<UUID, User> connectedUser, User user, HashMap<UUID, List<UUID>> peerMap) {
        LinkedList<UUID> peers = new LinkedList<>();
        for (Entity entity : player.getNearbyEntities(maxDistance, maxDistance, maxDistance)) {
            if (entity.getType() != EntityType.PLAYER) continue;
            UUID id = entity.getUniqueId();
            if (connectedUser.containsKey(id)) peers.add(id);
        }
        peerMap.put(user.getPlayerId(), peers);
    }


}