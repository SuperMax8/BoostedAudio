package fr.supermax_8.boostedaudio.spigot.proximitychat;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.user.User;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
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
            calculateUsersPeers(userOnServer, peers -> {
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
            if (!offlinePlayer.isOnline()) {
                continue;
            }

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
            } else result.put(userId, new PlayerInfo(playersInside.stream().toList(), pLoc, user.isMuted()));
        }

        return new LayerInfo(result, layer.getId());
    }

    private void calculateUsersPeers(Map<UUID, User> connectedUser, Consumer<Map<UUID, List<UUID>>> afterMath) {
        BoostedAudioSpigot.getInstance().getScheduler().runNextTick(task -> {
            afterMath.accept(getPeerMap(connectedUser));
        });
    }

    private HashMap<UUID, List<UUID>> getPeerMap(Map<UUID, User> connectedUser) {
        // UUID OF A USER, LIST OF PEERS OF THE USER
        HashMap<UUID, List<UUID>> peerMap = new HashMap<>();
        AtomicInteger done = new AtomicInteger();
        int finish = 0;

        for (User user : connectedUser.values()) {
            Player player = Bukkit.getPlayer(user.getPlayerId());
            if (player == null) continue;
            finish++;
            if (BoostedAudioSpigot.getInstance().getFolia().isFolia())
                BoostedAudioSpigot.getInstance().getScheduler().runAtEntity(player, task -> {
                    getPeerPlayer(player, connectedUser, user, peerMap, done);
                });
            else
                getPeerPlayer(player, connectedUser, user, peerMap, done);
        }
        while (finish != done.get()) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return peerMap;
    }

    private void getPeerPlayer(Player player, Map<UUID, User> connectedUser, User user, HashMap<UUID, List<UUID>> peerMap, AtomicInteger done) {
        LinkedList<UUID> peers = new LinkedList<>();
        for (Entity entity : player.getNearbyEntities(maxDistance, maxDistance, maxDistance)) {
            if (entity.getType() != EntityType.PLAYER) continue;
            UUID id = entity.getUniqueId();
            if (connectedUser.containsKey(id)) peers.add(id);
        }
        peerMap.put(user.getPlayerId(), peers);
        done.getAndIncrement();
    }


}