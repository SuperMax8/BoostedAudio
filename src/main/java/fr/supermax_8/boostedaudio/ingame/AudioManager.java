package fr.supermax_8.boostedaudio.ingame;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.BoostedAudioLoader;
import fr.supermax_8.boostedaudio.utils.SerializableLocation;
import fr.supermax_8.boostedaudio.web.Audio;
import fr.supermax_8.boostedaudio.web.ConnectionManager;
import fr.supermax_8.boostedaudio.web.PacketList;
import fr.supermax_8.boostedaudio.web.User;
import fr.supermax_8.boostedaudio.web.packets.AddPeerPacket;
import fr.supermax_8.boostedaudio.web.packets.RemovePeerPacket;
import fr.supermax_8.boostedaudio.web.packets.UpdateVocalPositionsPacket;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class AudioManager extends BukkitRunnable {

    private static final ConnectionManager manager = BoostedAudio.getInstance().manager;
    private static final double maxDistance = BoostedAudio.getInstance().getConfiguration().getMaxVoiceDistance();
    private static final Map<UUID, User> users = manager.getUsers();

    private final RegionManager regionManager;
    private final SpeakerManager speakerManager;
    private File data;

    public AudioManager() {
        regionManager = RegionManager.create();
        speakerManager = new SpeakerManager();
        loadData();
    }

    public void loadData() {
        data = new File(BoostedAudioLoader.getInstance().getDataFolder(), "data");
        data.mkdirs();

        BoostedAudio.debug("Loading data...");
        if (regionManager != null) {
            FileConfiguration regions = YamlConfiguration.loadConfiguration(new File(data, "regions.yml"));
            regions.getKeys(false).forEach(s -> {
                ConfigurationSection section = regions.getConfigurationSection(s);
                String region = section.getString("region");
                Audio audio = parseAudio(section.getConfigurationSection("audio"));
                if (audio == null) return;
                regionManager.addRegion(region, audio);
                BoostedAudio.debug("Loaded region: " + region);
            });
        }

        FileConfiguration speakersConfig = YamlConfiguration.loadConfiguration(new File(data, "speakers.yml"));
        for (String key : speakersConfig.getKeys(false)) {
            ConfigurationSection section = speakersConfig.getConfigurationSection(key);
            Audio audio = parseAudio(section.getConfigurationSection("audio")); // Vous devez également implémenter la logique pour analyser l'audio.
            if (audio == null) continue;
            speakerManager.addSpeaker(audio);
            BoostedAudio.debug("Loaded speaker: " + audio.getId());
        }
    }

    public void saveData() {
        BoostedAudio.info("Saved...");
        if (regionManager != null) {
            File regionFile = new File(data, "regions.yml");
            FileConfiguration regions = YamlConfiguration.loadConfiguration(regionFile);
            regions.getKeys(false).forEach(s -> regions.set(s, null));
            int count = 0;
            for (Map.Entry<String, Audio> entry : regionManager.getAudioRegions().entrySet()) {
                String region = entry.getKey();
                Audio audio = entry.getValue();
                regions.set(count + ".region", region);
                saveAudio(regions.createSection(count + ".audio"), audio);
                count++;
            }
            try {
                regions.save(regionFile);
            } catch (Exception e) {
            }
        }

        File speakerFile = new File(data, "speakers.yml");
        FileConfiguration speakers = YamlConfiguration.loadConfiguration(speakerFile);
        speakers.getKeys(false).forEach(s -> speakers.set(s, null));
        int count = 0;
        for (Map.Entry<Location, Audio> entry : speakerManager.speakers.entrySet()) {
            ConfigurationSection section = speakers.createSection(count + "");
            Location location = entry.getKey();
            section.set("world", location.getWorld().getName());
            section.set("x", location.getX());
            section.set("y", location.getY());
            section.set("z", location.getZ());
            Audio audio = entry.getValue();
            saveAudio(section.createSection("audio"), audio);
            count++;
        }
        try {
            speakers.save(speakerFile);
        } catch (Exception e) {
        }
        BoostedAudio.info("Saved!");
    }

    @Override
    public void run() {
        try {
            /*long ts1 = Bukkit.getServer().getWorlds().get(0).getTime();*/
            HashMap<UUID, User> connectedUsers = getConnectedUserAndClean();
            if (regionManager != null) regionManager.tick(connectedUsers);

            Map<UUID, List<UUID>> peersMap = calculateUsersPeers(connectedUsers);
            HashSet<PeerConnection> toLink = new HashSet<>();
            HashSet<PeerConnection> toUnLink = new HashSet<>();

            fillLinkUnlink(toLink, toUnLink, connectedUsers, peersMap);

            toLink.forEach(PeerConnection::link);
            toUnLink.forEach(PeerConnection::unLink);

            sendUpdatePositions(connectedUsers, peersMap);

/*            long ts2 = Bukkit.getServer().getWorlds().get(0).getTime();

            if (ts1 != ts2) {
                System.out.println("§cPROBLEMMMMMEEEEEE !!!!!!");
                System.out.println(ts1);
                System.out.println(ts2);
            }*/
        } catch (CancellationException e) {
            // Do nothing
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Audio parseAudio(ConfigurationSection section) {
        List<String> link = section.getStringList("link");
        if (link.isEmpty()) {
            BoostedAudio.info("No audio links found in the configuration file. !!");
            return null;
        }
        Audio.AudioSpatialInfo spatialInfo = null;
        if (section.contains("spatialInfo")) {
            String s = section.getString("spatialInfo.distanceModel");
            if (s == null) {
                spatialInfo = new Audio.AudioSpatialInfo(
                        new SerializableLocation(
                                (float) section.getDouble("spatialInfo.x"),
                                (float) section.getDouble("spatialInfo.y"),
                                (float) section.getDouble("spatialInfo.z"),
                                0,
                                section.getString("spatialInfo.world")
                        ),
                        section.getDouble("spatialInfo.maxVoiceDistance")
                );
            } else {
                spatialInfo = new Audio.AudioSpatialInfo(
                        new SerializableLocation(
                                (float) section.getDouble("spatialInfo.x"),
                                (float) section.getDouble("spatialInfo.y"),
                                (float) section.getDouble("spatialInfo.z"),
                                0,
                                section.getString("spatialInfo.world")
                        ),
                        section.getDouble("spatialInfo.maxVoiceDistance"),
                        section.getString("spatialInfo.distanceModel"),
                        section.getDouble("spatialInfo.refDistance"),
                        section.getDouble("spatialInfo.rolloffFactor")
                );
            }
        }
        int fadeIn = section.getInt("fadeIn");
        int fadeOut = section.getInt("fadeOut");
        boolean loop = section.getBoolean("loop");

        return new Audio(link, spatialInfo, UUID.randomUUID(), fadeIn, fadeOut, loop);
    }

    private void saveAudio(ConfigurationSection section, Audio audio) {
        section.set("link", audio.getLinks());

        Audio.AudioSpatialInfo spatialInfo = audio.getSpatialInfo();
        if (spatialInfo != null) {
            section.set("spatialInfo.x", spatialInfo.getLocation().getX());
            section.set("spatialInfo.y", spatialInfo.getLocation().getY());
            section.set("spatialInfo.z", spatialInfo.getLocation().getZ());
            section.set("spatialInfo.world", spatialInfo.getLocation().getWorld());
            section.set("spatialInfo.maxVoiceDistance", spatialInfo.getMaxVoiceDistance());
            section.set("spatialInfo.distanceModel", spatialInfo.getDistanceModel());
            section.set("spatialInfo.refDistance", spatialInfo.getRefDistance());
            section.set("spatialInfo.rolloffFactor", spatialInfo.getRolloffFactor());
        }

        section.set("fadeIn", audio.getFadeIn());
        section.set("fadeOut", audio.getFadeOut());
        section.set("loop", audio.isLoop());
    }


    private void fillLinkUnlink(Set<PeerConnection> toLink, Set<PeerConnection> toUnLink, Map<UUID, User> connectedUsers, Map<UUID, List<UUID>> peersMap) {
        for (User user : connectedUsers.values()) {
            List<UUID> currentPeersOfUser = peersMap.get(user.getPlayerId());
            Set<UUID> oldPeersOfUser = user.getRemotePeers();

            if (currentPeersOfUser == null || oldPeersOfUser == null) continue;

            // Player to add
            for (UUID peer : currentPeersOfUser) {
                if (!oldPeersOfUser.contains(peer))
                    toLink.add(new PeerConnection(user.getPlayerId(), connectedUsers.get(peer).getPlayerId()));
            }

            // Player to remove
            for (UUID peer : oldPeersOfUser) {
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
                try {
                    user.getSession().close();
                } catch (Exception e) {
                }
                BoostedAudio.debug("getConnectedUserAndClean close() session");
                continue;
            }
            userList.put(user.getPlayerId(), user);
        }
        return userList;
    }

    private Map<UUID, List<UUID>> calculateUsersPeers(HashMap<UUID, User> connectedUser) throws ExecutionException, InterruptedException {
        return Bukkit.getScheduler().callSyncMethod(BoostedAudioLoader.getInstance(), () -> {
            // UUID OF A USER, LIST OF PEERS OF THE USER
            HashMap<UUID, List<UUID>> peerMap = new HashMap<>();
            for (User user : connectedUser.values()) {
                Player player = Bukkit.getPlayer(user.getPlayerId());
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


    public RegionManager getRegionManager() {
        return regionManager;
    }

    public SpeakerManager getSpeakerManager() {
        return speakerManager;
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