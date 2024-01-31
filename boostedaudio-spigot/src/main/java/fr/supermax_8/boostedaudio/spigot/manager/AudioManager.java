package fr.supermax_8.boostedaudio.spigot.manager;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.utils.SerializableLocation;
import fr.supermax_8.boostedaudio.api.user.Audio;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AudioManager {

    @Getter
    private final RegionManager regionManager;
    @Getter
    private final SpeakerManager speakerManager;
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
        loadData();
    }

     public void loadData() {
        data = new File(BoostedAudioSpigot.getInstance().getDataFolder(), "data");
        data.mkdirs();

        BoostedAudioAPI.api.debug("Loading data...");
        if (regionManager != null) {
            FileConfiguration regions = YamlConfiguration.loadConfiguration(new File(data, "regions.yml"));
            regions.getKeys(false).forEach(s -> {
                ConfigurationSection section = regions.getConfigurationSection(s);
                String region = section.getString("region");
                Audio audio = parseAudio(section.getConfigurationSection("audio"));
                if (audio == null) return;
                regionManager.addRegion(region, audio);
                BoostedAudioAPI.api.debug("Loaded region: " + region);
            });
        }

        FileConfiguration speakersConfig = YamlConfiguration.loadConfiguration(new File(data, "speakers.yml"));
        for (String key : speakersConfig.getKeys(false)) {
            ConfigurationSection section = speakersConfig.getConfigurationSection(key);
            Audio audio = parseAudio(section.getConfigurationSection("audio"));
            if (audio == null) continue;
            speakerManager.addSpeaker(audio);
            BoostedAudioAPI.api.debug("Loaded speaker: " + audio.getId());
        }
    }

    public void saveData() {
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
    }

    private Audio parseAudio(ConfigurationSection section) {
        List<String> link = section.getStringList("link");
        if (link.isEmpty()) {
            BoostedAudioAPI.api.info("No audio links found in the configuration file. !!");
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
        boolean syncronous = section.getBoolean("syncronous");

        return new Audio(link, spatialInfo, UUID.randomUUID(), fadeIn, fadeOut, loop, syncronous);
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
        section.set("syncronous", audio.isSyncronous());
    }


}