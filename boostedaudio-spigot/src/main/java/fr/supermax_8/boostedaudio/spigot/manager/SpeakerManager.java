package fr.supermax_8.boostedaudio.spigot.manager;

import java.io.File;
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
    public final ConcurrentHashMap<Location, Audio> speakers = new ConcurrentHashMap<>();
    private FileConfiguration speakersConfig;

    public SpeakerManager() {
    }

    public void load(File dataFolder) {
        speakers.clear();
        speakersConfig = YamlConfiguration.loadConfiguration(new File(dataFolder, "speakers.yml"));
        for (String key : speakersConfig.getKeys(false)) {
            ConfigurationSection section = speakersConfig.getConfigurationSection(key);
            Audio audio = AudioManager.parseAudio(section.getConfigurationSection("audio"));
            if (audio == null) continue;
            addSpeaker(audio, false);
            BoostedAudioAPI.api.debug("Loaded speaker: " + audio.getId());
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

            speakersConfig
        }
    }

    

    public void removeSpeaker(Location location, boolean removeInConfig) {
        manager.removeAround(location);
        speakers.remove(new Location(location.getWorld(), location.getX(), location.getY(), location.getZ()));

    }

}