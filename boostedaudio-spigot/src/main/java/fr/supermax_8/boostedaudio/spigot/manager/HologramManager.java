package fr.supermax_8.boostedaudio.spigot.manager;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import fr.supermax_8.boostedaudio.api.audio.Audio;
import fr.supermax_8.boostedaudio.api.audio.Audio.AudioSpatialInfo;
import fr.supermax_8.boostedaudio.core.utils.SerializableLocation;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.gui.SpeakerEditGUI;
import fr.supermax_8.boostedaudio.spigot.hooks.holograms.HD3Hologram;
import fr.supermax_8.boostedaudio.spigot.hooks.holograms.Hologram;
import fr.supermax_8.boostedaudio.spigot.hooks.holograms.HologramType;
import fr.supermax_8.boostedaudio.spigot.utils.XMaterial;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Thanks to Ender_Grifeur99 for PR contrib for this feature
public class HologramManager implements Listener {

    @Getter
    private List<Player> playerList = new ArrayList<>();
    @Getter
    private Map<UUID, HologramType<?>> holos = new ConcurrentHashMap<>();
    @Getter
    private WrappedTask wrappedTask;
    private final JavaPlugin instance = BoostedAudioSpigot.getInstance();
    private final LinkedList<UUID> audioswaiting = new LinkedList<>();

    public HologramManager(SpeakerManager sm) {
        sm.getSpeakers().forEach(this::checkSpeaker);
        Runnable r = () -> {
            if (playerList.isEmpty())
                return;

            new ConcurrentHashMap<>(holos).forEach((s, holo) -> {
                if (holo != null && !holo.isDeleted())
                    return;
                holos.remove(s);
            });

            sm.getSpeakers().values().parallelStream()
                    .filter(au -> !audioswaiting.contains(au.getId()) && !holos.containsKey(au.getId()))
                    .forEach(au -> Bukkit.getScheduler().runTask(instance,
                            () -> checkSpeaker(sm.getSpeakers().entrySet().stream()
                                    .filter(e -> e.getValue().getId().equals(au.getId()))
                                    .findFirst().get().getKey(), au)));

            holos.forEach((id, holo) -> {
                if (holo == null)
                    return;
                if (holo.isDeleted())
                    return;
                Location loc = sm.getSpeakers().entrySet().stream().filter(e -> e.getValue().getId().equals(id))
                        .findFirst().get().getKey();
                Audio audio = sm.getSpeakers().get(loc);
                if (audio == null) {
                    holo.delete();
                    return;
                }
                if (!loc.getChunk().isLoaded())
                    return;
                holo.teleport(new Location(loc.getWorld(), loc.getBlockX() + 0.5,
                        loc.getBlockY() + 1.75 + (holo.size() * 0.25), loc.getBlockZ() + 0.5));

                int i = 0;
                for (String line : createHoloLines(audio)) {
                    setLine(holo, i, line);
                    i++;
                }
            });
        };
        wrappedTask = BoostedAudioSpigot.getInstance().getHologramType() instanceof HD3Hologram ?
                BoostedAudioSpigot.getInstance().getScheduler().runTimer(r, 10, 10) :
                BoostedAudioSpigot.getInstance().getScheduler().runTimerAsync(r, 10, 10);

    }

    private void setLine(HologramType<?> holo, int i, String txt) {
        BoostedAudioSpigot.getInstance().getScheduler().runNextTick(t -> {
            if (holo.isDeleted())
                return;
            holo.setline(i, txt);
        });
    }

    private List<String> createHoloLines(Audio au) {
        AudioSpatialInfo asi = au.getSpatialInfo();
        SerializableLocation loc = asi.getLocation();
        return List.of(
                au.getId().toString(),
                "§6X: §a" + loc.getX() + " §6Y: §a" + loc.getY() + " §6Z: §a"
                        + loc.getZ() + " §6World: §a" + loc.getWorld(),
                "§6Loop: §a" + au.isLoop(),
                "§6Synchronous: §a" + au.isSynchronous(),
                "§6FadeIn: §a" + au.getFadeIn(),
                "§6FadeOut: §a" + au.getFadeOut(),
                "§6Distance Model: §a" + asi.getDistanceModel(),
                "§6MaxVoiceDistance: §a" + asi.getMaxVoiceDistance(),
                "§6RefDistance: §a" + asi.getRefDistance(),
                "§6getRolloffFactor: §a" + asi.getRolloffFactor(),
                "",
                "§6Links Or Playlist: §a" + (au.getPlayList().getId() == null ? au.getPlayList().getLinks() : au.getPlayList().getId())
        );
    }

    private void checkSpeaker(Location lc, Audio au) {
        if (!lc.getChunk().isLoaded()) {
            if (!audioswaiting.contains(au.getId()))
                audioswaiting.add(au.getId());
            BoostedAudioSpigot.getInstance().getScheduler().runLater(() -> checkSpeaker(lc, au), 20);
            return;
        }

        Location loc = new Location(lc.getWorld(), lc.getBlockX() + 0.5,
                lc.getBlockY() + 1.75, lc.getBlockZ() + 0.5);
        HologramType<?> holo = new Hologram(instance).getHolo();
        holo.createHologram(loc, false);
        holo.setPlayersVisible(playerList);

        for (String line : createHoloLines(au)) holo.appendTextLine(line);

        holo.appendItemLine(XMaterial.NOTE_BLOCK.parseItem());

        holo.interact(p -> BoostedAudioSpigot.getInstance().getScheduler()
                .runNextTick(t -> {
                    Audio currentAudio = BoostedAudioSpigot.getInstance().getAudioManager().getSpeakerManager().getSpeakers()
                            .entrySet().stream()
                            .filter(e -> e.getValue().getId().equals(au.getId()))
                            .findFirst().get().getValue();
                    new SpeakerEditGUI(p, null, currentAudio);
                }));
        holos.put(au.getId(), holo);
        audioswaiting.remove(au.getId());
    }

}