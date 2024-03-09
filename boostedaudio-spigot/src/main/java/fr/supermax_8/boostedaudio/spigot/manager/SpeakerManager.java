package fr.supermax_8.boostedaudio.spigot.manager;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.api.audio.Audio;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.utils.AroundManager;
import fr.supermax_8.boostedaudio.spigot.utils.InternalUtils;
import lombok.Getter;
import org.bukkit.Location;

import java.util.concurrent.ConcurrentHashMap;

public class SpeakerManager {

    private final AroundManager manager = BoostedAudioSpigot.getInstance().getAroundManager();
    @Getter
    public final ConcurrentHashMap<Location, Audio> speakers = new ConcurrentHashMap<>();

    public SpeakerManager() {
    }

    public void addSpeaker(Audio audio) {
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
    }

    public void removeSpeaker(Location location) {
        manager.removeAround(location);
        speakers.remove(new Location(location.getWorld(), location.getX(), location.getY(), location.getZ()));
    }

}