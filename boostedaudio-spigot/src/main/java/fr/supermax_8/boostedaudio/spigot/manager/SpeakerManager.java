package fr.supermax_8.boostedaudio.spigot.manager;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.api.audio.Audio;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.utils.AroundManager;
import fr.supermax_8.boostedaudio.spigot.utils.InternalUtils;
import lombok.Getter;

public class SpeakerManager {

    private final AroundManager manager = BoostedAudioSpigot.getInstance().getAroundManager();
    @Getter
    private final ConcurrentHashMap<Location, Audio> speakers = new ConcurrentHashMap<>();
    private FileConfiguration speakersConfig;
    private File speakerConfigFile;

    public SpeakerManager() {
    }

    public void load(File dataFolder) {
        speakers.clear();
        speakerConfigFile = new File(dataFolder, "speakers.yml");
        speakersConfig = YamlConfiguration.loadConfiguration(speakerConfigFile);
        Set<String> keys = speakersConfig.getKeys(false);
        if (keys.contains("0")) {
            convertToV2();
            keys = speakersConfig.getKeys(false);
        }

        for (String key : keys) {
            ConfigurationSection section = speakersConfig.getConfigurationSection(key);
            try {
                Audio audio = AudioManager.parseAudio(section.getConfigurationSection("audio"));
                if (audio == null) continue;
                addSpeaker(audio, false);
                BoostedAudioAPI.api.debug("Loaded speaker: " + audio.getId());
            } catch (Exception e) {
                BoostedAudioAPI.api.info("Problem while loading speaker: " + section.getName() + ", please check the config of it");
                e.printStackTrace();
            }
        }
    }

    private void convertToV2() {
        speakersConfig.getKeys(false).forEach(key -> {
            ConfigurationSection oldsection = speakersConfig.getConfigurationSection(key);
            Audio audio = AudioManager.parseAudio(oldsection.getConfigurationSection("audio"));
            speakersConfig.set(key, null);
            ConfigurationSection section = speakersConfig.createSection(audio.getSpatialInfo().getLocation().toString());
            AudioManager.saveAudio(section.createSection("audio"), audio);
        });
        try {
            speakersConfig.save(speakerConfigFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addSpeaker(Audio audio, boolean saveInConfig) {
        Location location = InternalUtils.serializableLocToBukkitLocation(audio.getSpatialInfo().getLocation());
        speakers.put(new Location(location.getWorld(), location.getX(), location.getY(), location.getZ()), audio);
        manager.addAround(location, audio.getSpatialInfo().getMaxVoiceDistance(),
                p -> {
                    User user = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(p.getUniqueId());
                    if (user == null) return;
                    user.playAudio(audio);
                },
                p -> {
                    User user = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(p.getUniqueId());
                    if (user == null) return;
                    user.stopAudio(audio);
                },
                p -> BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().containsKey(p.getUniqueId())
        );
        if (saveInConfig) {
            AudioManager.saveAudio(speakersConfig.createSection(audio.getSpatialInfo().getLocation().toString()), audio);
            try {
                speakersConfig.save(speakerConfigFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void removeSpeaker(Location location, boolean removeInConfig) {
        manager.removeAround(location);
        speakers.remove(new Location(location.getWorld(), location.getX(), location.getY(), location.getZ()));
        if (removeInConfig) {
            speakersConfig.set(InternalUtils.bukkitLocationToSerializableLoc(location).toString(), null);
            try {
                speakersConfig.save(speakerConfigFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}