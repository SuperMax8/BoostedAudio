package fr.supermax_8.boostedaudio.spigot.manager;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.supermax_8.boostedaudio.api.audio.PlayList;

public class PlayListManager {

    private ConcurrentHashMap<String, PlayList> playlists = new ConcurrentHashMap<>();

    public void load(File dataFolder) {
        playlists.clear();
        File playlistFolder = new File(dataFolder, "playlist");
        if (!playlistFolder.exists())
            playlistFolder.mkdirs();
        
        for (PlayList playlist : getPlayListRecursivly(playlistFolder, new LinkedList<>())) {
            playlists.put(playlist.getId(), playlist);
        }
    }

    public PlayList get(String id) {
        return playlists.get(id);
    }
    

    private List<PlayList> getPlayListRecursivly(File folder, List<PlayList> list) {
        for (File f : folder.listFiles((f -> f.isDirectory() || f.getName().endsWith(".yml")))) {
            if (f.isDirectory())
                getPlayListRecursivly(f, list);
            else {
                FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
                for (String playlistId : fc.getKeys(false))
                    list.add(new PlayList(playlistId, fc.getStringList(playlistId)));
            }
        }
        return list;
    }

}