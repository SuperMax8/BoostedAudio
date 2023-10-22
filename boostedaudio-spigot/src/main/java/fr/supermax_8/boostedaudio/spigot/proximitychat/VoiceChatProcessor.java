package fr.supermax_8.boostedaudio.spigot.proximitychat;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.proximitychat.LayerInfo;
import fr.supermax_8.boostedaudio.core.proximitychat.PlayerInfo;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceChatResult;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceLayer;
import fr.supermax_8.boostedaudio.core.utils.SerializableLocation;
import fr.supermax_8.boostedaudio.core.websocket.User;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.utils.InternalUtils;
import fr.supermax_8.boostedaudio.spigot.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class VoiceChatProcessor {

    // LayerId : Layer
    private final ConcurrentHashMap<String, VoiceLayer> layers = new ConcurrentHashMap<>();

    private static final double maxDistance = BoostedAudioAPI.api.getConfiguration().getMaxVoiceDistance();

    public VoiceChatProcessor() {
    }

    public VoiceChatResult process() {
        Map<UUID, User> userOnServer = BoostedAudioAPI.api.getHostProvider().getPlayersOnServer();

        System.out.println("userOnServer = " + userOnServer);
        // Calculate peers
        Map<UUID, List<UUID>> peers;
        try {
            peers = calculateUsersPeers(userOnServer);
            System.out.println("peers = " + peers);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        List<LayerInfo> layerInfos = new ArrayList<>();
        // Process every layers
        for (Map.Entry<String, VoiceLayer> entry : layers.entrySet()) {
            VoiceLayer layer = entry.getValue();
            layerInfos.add(processLayer(layer, userOnServer, peers));
        }
        return new VoiceChatResult(layerInfos);
    }

    private LayerInfo processLayer(VoiceLayer layer, Map<UUID, User> userOnServer, Map<UUID, List<UUID>> peers) {
        // Layer cached
        Predicate<UUID> predicate = layer.getPlayerInLayer();
        Set<UUID> playersInside = layer.getPlayersInside();

        // Result of the layer
        Map<UUID, PlayerInfo> result = new HashMap<>();

        System.out.println("playersInside = " + playersInside);

        // Check every user on server
        for (User user : userOnServer.values()) {
            UUID userId = user.getPlayerId();
            System.out.println("AHHHHH: userId = " + userId);
            if (predicate != null) {
                if (predicate.test(user.getPlayerId())) playersInside.add(userId);
                else playersInside.remove(userId);
            } else playersInside.add(userId);

            SerializableLocation pLoc = InternalUtils.bukkitLocationToSerializableLoc(Bukkit.getPlayer(userId).getLocation());
            // Put result
            if (layer.isAudioSpatialized()) {
                List<UUID> globalPeersOfUser = peers.get(userId);
                System.out.println("globalPeersOfUser = " + globalPeersOfUser);
                for (UUID peer : globalPeersOfUser) {
                    if (playersInside.contains(peer))
                        result.computeIfAbsent(userId, k -> new PlayerInfo(pLoc)).getPeers().add(peer);
                }
            } else result.put(userId, new PlayerInfo(playersInside.stream().toList(), pLoc));
            /*System.out.println("userLayerPeers= " + result.get(userId).getPeers());*/
        }

        return new LayerInfo(result, layer.getId());
    }

    private Map<UUID, List<UUID>> calculateUsersPeers(Map<UUID, User> connectedUser) throws ExecutionException, InterruptedException {
        return Bukkit.getScheduler().callSyncMethod(BoostedAudioSpigot.getInstance(), () -> {
            // UUID OF A USER, LIST OF PEERS OF THE USER
            HashMap<UUID, List<UUID>> peerMap = new HashMap<>();
            System.out.println("Testtt");
            for (User user : connectedUser.values()) {
                Player player = Bukkit.getPlayer(user.getPlayerId());
                System.out.println(player.getName());
                if (player == null) continue;
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


    public ConcurrentHashMap<String, VoiceLayer> getLayers() {
        return layers;
    }


}