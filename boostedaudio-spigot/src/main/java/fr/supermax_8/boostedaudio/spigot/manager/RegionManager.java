package fr.supermax_8.boostedaudio.spigot.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.api.audio.Audio;
import lombok.Getter;

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
                    return;
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
            RegionInfo regionInfo = infoMap.get(pId);
            if (regionInfo == null)
                continue;
            if (user == null) {
                infoMap.put(pId, new RegionInfo());
                continue;
            }

            List<IWrappedRegion> lastRegions = regionInfo.getLastRegions();
            Set<IWrappedRegion> playerRegions = api.getRegions(p.getLocation());
            // Found highest priority regions
            List<IWrappedRegion> highestPriorityRegions = new ArrayList<>();
            int highestPriority = Integer.MIN_VALUE;

            for (IWrappedRegion region : playerRegions) {
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

            HashSet<String> lastRegionsString = new HashSet<>();
            for (IWrappedRegion region : lastRegions)
                lastRegionsString.add(region.getId());

            HashSet<String> highestPriorityRegionsString = new HashSet<>();
            for (IWrappedRegion region : highestPriorityRegions)
                highestPriorityRegionsString.add(region.getId());

            for (String region : highestPriorityRegionsString) {
                if (!lastRegionsString.contains(region)) {
                    // To add
                    Audio audio = audioRegions.get(region);
                    if (audio != null)
                        user.playAudio(audio);
                }
            }

            for (String region : lastRegionsString) {
                if (!highestPriorityRegionsString.contains(region)) {
                    // To remove
                    Audio audio = audioRegions.get(region);
                    if (audio != null)
                        user.stopAudio(audio);
                }
            }

            lastRegions.clear();
            lastRegions.addAll(highestPriorityRegions);
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

        private List<IWrappedRegion> lastRegions = new ArrayList<>();

        public List<IWrappedRegion> getLastRegions() {
            return lastRegions;
        }

        public void setLastRegions(List<IWrappedRegion> lastRegions) {
            this.lastRegions = lastRegions;
        }

    }

}