package fr.supermax_8.boostedaudio.spigot.manager;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
    private HologramManager hm;

    public SpeakerManager() {

    }

    public void load(File dataFolder) {
        speakers.clear();
        speakerConfigFile = new File(dataFolder, "speakers.yml");
        speakersConfig = YamlConfiguration.loadConfiguration(speakerConfigFile);
        Set<String> keys = speakersConfig.getKeys(false);

        try {
            Optional<String> first = keys.stream().findFirst();
            first.ifPresent(UUID::fromString);
        } catch (Exception e) {
            convertToV3();
            speakersConfig = YamlConfiguration.loadConfiguration(speakerConfigFile);
            keys = speakersConfig.getKeys(false);
            System.out.println("Convert speakers...");
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
        if (BoostedAudioSpigot.ishologramInstalled()) hm = new HologramManager(this);
    }

    private void convertToV3() {
        speakersConfig.getKeys(false).forEach(key -> {
            ConfigurationSection oldsection = speakersConfig.getConfigurationSection(key);
            Audio audio = AudioManager.parseAudio(oldsection.getConfigurationSection("audio"));
            if (audio == null) return;
            speakersConfig.set(key, null);
            ConfigurationSection section = speakersConfig.createSection(audio.getId().toString());
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
        Objects.requireNonNull(audio, "Weird behavior, contact the dev");
        Objects.requireNonNull(location.getWorld(), "World can't be null, please fix the world name with a valid world in the data file for this speaker");
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
            AudioManager.saveAudio(speakersConfig.createSection(audio.getId().toString()).createSection("audio"), audio);
            try {
                speakersConfig.save(speakerConfigFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void removeSpeaker(Location location, boolean removeInConfig) {
        manager.removeAround(location);
        Audio audio = speakers.remove(new Location(location.getWorld(), location.getX(), location.getY(), location.getZ()));
        if (removeInConfig) {
            speakersConfig.set(audio.getId().toString(), null);
            try {
                speakersConfig.save(speakerConfigFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public HologramManager getHologramManager() {
        return hm;
    }

}