package fr.supermax_8.boostedaudio.spigot.manager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.tcoded.folialib.wrapper.task.WrappedTask;

import fr.supermax_8.boostedaudio.api.audio.Audio;
import fr.supermax_8.boostedaudio.api.audio.Audio.AudioSpatialInfo;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.gui.SpeakerEditGUI;
import fr.supermax_8.boostedaudio.spigot.hooks.holograms.Hologram;
import fr.supermax_8.boostedaudio.spigot.hooks.holograms.HologramType;
import fr.supermax_8.boostedaudio.spigot.utils.XMaterial;

//@SuppressWarnings("unchecked")
public class HologramManager implements Listener {

    private List<Player> pls = new ArrayList<>();
    private Map<UUID, HologramType<?>> holos = new ConcurrentHashMap<>();
    private WrappedTask wrappedTask;
    private JavaPlugin instance = BoostedAudioSpigot.getInstance();
    private LinkedList<UUID> audioswaiting = new LinkedList<>();
    // private SpeakerManager sm;

    public HologramManager(SpeakerManager sm) {
        // this.sm = sm;
        sm.getSpeakers().forEach(this::checkSpeaker);
        wrappedTask = BoostedAudioSpigot.getInstance().getScheduler().runTimerAsync(() -> {
            if (pls.isEmpty())
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
                AudioSpatialInfo asi = audio.getSpatialInfo();
                setLine(holo, 0, audio.getId().toString());
                setLine(holo, 1, "§6X: §a" + loc.getBlockX() + " §6Y: §a" + loc.getBlockY() + " §6Z: §a"
                        + loc.getBlockZ() + " §6World: §a" + loc.getWorld().getName());
                setLine(holo, 2, "§6FadeIn: §a" + audio.getFadeIn());
                setLine(holo, 3, "§6FadeOut: §a" + audio.getFadeOut());
                setLine(holo, 4, "§6Distance Model: §a" + asi.getDistanceModel());
                setLine(holo, 5, "§6MaxVoiceDistance: §a" + asi.getMaxVoiceDistance());
                setLine(holo, 6, "§6RefDistance: §a" + asi.getRefDistance());
                setLine(holo, 7, "§6getRolloffFactor: §a" + asi.getRolloffFactor());
            });
        }, 10, 10);

    }

    // private String toReadable(String string) {
    // StringBuilder builder = new StringBuilder();
    // for (String s : string.split("_"))
    // builder.append(s.substring(0, 1)).append(s.substring(1).toLowerCase());
    // return builder.toString();
    // }

    private void setLine(HologramType<?> holo, int i, String txt) {
        BoostedAudioSpigot.getInstance().getScheduler().runNextTick(t -> {
            if (holo.isDeleted())
                return;
            holo.setline(i, txt);
        });
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
        holo.setPlayersVisible(pls);
        AudioSpatialInfo asi = au.getSpatialInfo();

        holo.appendTextLine(au.getId().toString());
        holo.appendTextLine("§6X: §a" + loc.getBlockX() + " §6Y: §a" + loc.getBlockY() + " §6Z: §a" + loc.getBlockZ()
                + " §6World: §a" + loc.getWorld().getName());
        holo.appendTextLine("§6FadeIn: §a" + au.getFadeIn());
        holo.appendTextLine("§6FadeOut: §a" + au.getFadeOut());
        holo.appendTextLine("§6Distance Model: §a" + asi.getDistanceModel());
        holo.appendTextLine("§6MaxVoiceDistance: §a" + asi.getMaxVoiceDistance());
        holo.appendTextLine("§6RefDistance: §a" + asi.getRefDistance());
        holo.appendTextLine("§6getRolloffFactor: §a" + asi.getRolloffFactor());
        holo.appendItemLine(XMaterial.NOTE_BLOCK.parseItem());
        holo.interact(p -> {
            StringJoiner linksJoiner = new StringJoiner(";");
            String playlistId = au.getPlayList().getId();
            if (playlistId == null)
                for (String s : au.getPlayList().getLinks())
                    linksJoiner.add(s);
            else
                linksJoiner.add(playlistId);
            BoostedAudioSpigot.getInstance().getScheduler()
                    .runNextTick(t -> new SpeakerEditGUI(p, null, au));
        });
        holos.put(au.getId(), holo);
        audioswaiting.remove(au.getId());
    }

    public WrappedTask getWrappedTask() {
        return wrappedTask;
    }

    public Map<UUID, HologramType<?>> getHolos() {
        return holos;
    }

    public List<Player> getPlayerList() {
        return pls;
    }

}
