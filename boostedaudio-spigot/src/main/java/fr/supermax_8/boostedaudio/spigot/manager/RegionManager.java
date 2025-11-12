package fr.supermax_8.boostedaudio.spigot.manager;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.api.audio.Audio;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RegionManager {

    @Getter
    private static final WorldGuardWrapper api = WorldGuardWrapper.getInstance();
    @Getter
    private final HashMap<UUID, RegionInfo> infoMap = new HashMap<>();

    private final ConcurrentHashMap<String, Audio> audioRegions = new ConcurrentHashMap<>();
    private FileConfiguration regionsConfig;
    private File regionsConfigFile;

    private String data = "%%__USER__%% %%__RESOURCE__%% %%__NONCE__%%";

    private RegionManager() {

    }

    public void load(File dataFolder) {
        audioRegions.clear();
        regionsConfigFile = new File(dataFolder, "regions.yml");
        regionsConfig = YamlConfiguration.loadConfiguration(regionsConfigFile);
        if (regionsConfig.getKeys(false).contains("0")) convertToV2();

        for (String region : regionsConfig.getKeys(false)) {
            try {
                ConfigurationSection section = regionsConfig.getConfigurationSection(region);
                Audio audio = AudioManager.parseAudio(section.getConfigurationSection("audio"));
                if (audio == null)
                    continue;
                addRegion(region, audio, false);
                BoostedAudioAPI.api.debug("Loaded region: " + region);
            } catch (Exception e) {
                BoostedAudioAPI.api.info("Problem while loading region audio: " + region + ", please check the config of it");
                e.printStackTrace();
            }
        }
    }

    private void convertToV2() {
        regionsConfig.getKeys(false).forEach(key -> {
            ConfigurationSection oldsection = regionsConfig.getConfigurationSection(key);
            String region = oldsection.getString("region");
            Audio audio = AudioManager.parseAudio(oldsection.getConfigurationSection("audio"));
            if (audio == null) return;
            regionsConfig.set(key, null);
            ConfigurationSection section = regionsConfig.createSection(region);
            AudioManager.saveAudio(section.createSection("audio"), audio);
        });
        try {
            regionsConfig.save(regionsConfigFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tick(Map<UUID, User> connectedUsers) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            UUID pId = p.getUniqueId();
            User user = connectedUsers.get(pId);
            if (user == null) {
                // Reset if not connected to audio
                infoMap.remove(pId);
                continue;
            }
            RegionInfo regionInfo = infoMap.computeIfAbsent(pId, k -> new RegionInfo());

            List<IWrappedRegion> playerLastPlayingRegions = regionInfo.getLastRegions();
            Set<IWrappedRegion> currentPlayerRegions = api.getRegions(p.getLocation());
            // Found highest priority regions meaning the regions that the player should be now hearing
            List<IWrappedRegion> highestPriorityRegions = new ArrayList<>();
            int highestPriority = Integer.MIN_VALUE;

            for (IWrappedRegion region : currentPlayerRegions) {
                if (!audioRegions.containsKey(region.getId()))
                    continue;
                int priority = region.getPriority();
                if (priority > highestPriority) {
                    highestPriority = priority;
                    highestPriorityRegions.clear();
                }
                if (priority == highestPriority)
                    highestPriorityRegions.add(region);
            }

            HashSet<String> playerLastPlayingRegionsString = new HashSet<>();
            for (IWrappedRegion region : playerLastPlayingRegions)
                playerLastPlayingRegionsString.add(region.getId());

            HashSet<String> highestPriorityRegionsString = new HashSet<>();
            for (IWrappedRegion region : highestPriorityRegions)
                highestPriorityRegionsString.add(region.getId());

            for (String region : highestPriorityRegionsString) {
                if (!playerLastPlayingRegionsString.contains(region)) {
                    // To add
                    Audio audio = audioRegions.get(region);
                    if (audio != null)
                        user.playAudio(audio);
                }
            }

            for (String region : playerLastPlayingRegionsString) {
                if (!highestPriorityRegionsString.contains(region)) {
                    // To remove
                    Audio audio = audioRegions.get(region);
                    if (audio != null)
                        user.stopAudio(audio);
                }
            }

            playerLastPlayingRegions.clear();
            playerLastPlayingRegions.addAll(highestPriorityRegions);
        }
    }

    public void addRegion(String region, Audio audio, boolean addInConfig) {
        audioRegions.put(region, audio);
        if (addInConfig) {
            ConfigurationSection section = regionsConfig.createSection(region);
            AudioManager.saveAudio(section.createSection("audio"), audio);
            try {
                regionsConfig.save(regionsConfigFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeRegion(String region, boolean removeInConfig) {
        audioRegions.remove(region);
        if (removeInConfig) {
            regionsConfig.set(region, null);
            try {
                regionsConfig.save(regionsConfigFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static RegionManager create() {
        if (api.getWorldGuardPlugin() == null || !api.getWorldGuardPlugin().isEnabled())
            return null;
        return new RegionManager();
    }

    public Map<String, Audio> getAudioRegions() {
        return audioRegions;
    }

    public static class RegionInfo {

        @Getter
        private List<IWrappedRegion> lastRegions = new ArrayList<>();

    }

}