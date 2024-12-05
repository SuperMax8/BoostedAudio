package fr.supermax_8.boostedaudio.spigot.manager;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.supermax_8.boostedaudio.api.audio.PlayList;

public class PlayListManager {

    private final ConcurrentHashMap<String, PlayList> playlists = new ConcurrentHashMap<>();

    public void load(File dataFolder) {
        playlists.clear();
        File playlistFolder = new File(dataFolder, "playlist");
        if (!playlistFolder.exists())
            playlistFolder.mkdirs();

        for (PlayList playlist : getPlayListRecursivly(playlistFolder, new LinkedList<>())) {
            playlists.put(playlist.getId(), playlist);
            BoostedAudioAPI.api.debug("Loaded playlist: " + playlist.getId());
        }
    }

    public PlayList get(String id) {
        return playlists.get(id.toLowerCase());
    }

    public void remove(String id) {
        playlists.remove(id.toLowerCase());
    }

    public void addPlayList(PlayList playlist) {
        if (playlist.getId() == null) throw new RuntimeException("Trying to add a playlist without playlist Id");
        playlists.put(playlist.getId(), playlist);
    }

    private List<PlayList> getPlayListRecursivly(File folder, List<PlayList> list) {
        // MADNESS, INSANITY but it works I'm and Ender are genius
        // 05/12/2024 - I JUST REREAD THIS IS STILL A GENIUS CODE!!!! - SuperMax_8
        for (File f : folder.listFiles((f -> {
            if (f.isDirectory()) {
                getPlayListRecursivly(f, list);
                return false;
            }
            return f.getName().endsWith(".yml");
        }))) {
            FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
            String id = f.getName().replace(".yml", "");
            list.add(new PlayList(id, fc.getStringList(id), fc.getBoolean("synchronous", false)));
        }
        return list;
    }

}