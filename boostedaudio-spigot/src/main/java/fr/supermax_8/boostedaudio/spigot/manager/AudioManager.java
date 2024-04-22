package fr.supermax_8.boostedaudio.spigot.manager;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.audio.Audio;
import fr.supermax_8.boostedaudio.api.audio.PlayList;
import fr.supermax_8.boostedaudio.core.utils.SerializableLocation;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import lombok.Getter;

public class AudioManager {

    @Getter
    private final RegionManager regionManager;
    @Getter
    private final SpeakerManager speakerManager;
    @Getter
    private final PlayListManager playListManager;
    private File data;
    private static AudioManager instance;

    public AudioManager() {
        instance = this;
        playListManager = new PlayListManager();
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
        BoostedAudioAPI.api.debug("Loading data...");
        data = new File(BoostedAudioSpigot.getInstance().getDataFolder(), "data");
        data.mkdirs();

        playListManager.load(data);
        speakerManager.load(data);
        if (regionManager != null) regionManager.load(data);
    }

    /**
     * Parse section in Audio
     * @param section - aka the audio section
     * @return the audio parsed
     */
    public static Audio parseAudio(ConfigurationSection section) {
        PlayList playList;
        if (section.contains("playlist")) {
            String playListId = section.getString("playlist");
            playList = instance.playListManager.get(playListId);
            if (playList == null) {
                BoostedAudioAPI.getAPI().info("Playlist " + playListId + " not found");
                playList = new PlayList(List.of());
            }
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