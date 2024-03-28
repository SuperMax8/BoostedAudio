package fr.supermax_8.boostedaudio.spigot.manager;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.utils.SerializableLocation;
import fr.supermax_8.boostedaudio.api.audio.Audio;
import fr.supermax_8.boostedaudio.api.audio.PlayList;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class AudioManager {

    @Getter
    private final RegionManager regionManager;
    @Getter
    private final SpeakerManager speakerManager;
    @Getter
    private final PlayListManager playListManager;
    private File data;

    public AudioManager() {
        RegionManager regionManager1;
        try {
            regionManager1 = RegionManager.create();
        } catch (Throwable e) {
            regionManager1 = null;
        }
        regionManager = regionManager1;
        speakerManager = new SpeakerManager();
        playListManager = new PlayListManager();
        loadData();
    }

     public void loadData() {
        BoostedAudioAPI.api.debug("Loading data...");
        data = new File(BoostedAudioSpigot.getInstance().getDataFolder(), "data");
        data.mkdirs();

        playListManager.load(data);
        speakerManager.load(data);
        if (regionManager != null) regionManager.load(data);
    }

/*     public void saveData() {
        BoostedAudioAPI.api.info("Saved...");
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
        BoostedAudioAPI.api.info("Saved!");
    } */

    public static Audio parseAudio(ConfigurationSection section) {
        PlayList playList;
        if (section.contains("playlist")) {
            playList = BoostedAudioSpigot.getInstance().getAudioManager().playListManager.get(section.getString("playlist"));
        } else {
            List<String> link = section.getStringList("link");
            if (link.isEmpty()) {
                BoostedAudioAPI.api.info("No audio links found in the configuration file. !!");
                return null;
            }
            playList = new PlayList(link);
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
        boolean synchronous = section.getBoolean("synchronous");

        return new Audio(playList, spatialInfo, UUID.randomUUID(), fadeIn, fadeOut, loop, synchronous);
    }

    public static void saveAudio(ConfigurationSection section, Audio audio) {
        PlayList list = audio.getPlayList();
        if (list.getId() == null) section.set("link", list.getLinks());
        else section.set("playlist", list.getId());

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
        section.set("synchronous", audio.isSynchronous());
    }


}