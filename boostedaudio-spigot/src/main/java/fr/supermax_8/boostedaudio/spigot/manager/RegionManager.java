package fr.supermax_8.boostedaudio.spigot.manager;

import fr.supermax_8.boostedaudio.api.user.User;
import fr.supermax_8.boostedaudio.api.user.Audio;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RegionManager {

    private static final WorldGuardWrapper api = WorldGuardWrapper.getInstance();
    private final HashMap<UUID, RegionInfo> infoMap = new HashMap<>();

    private final ConcurrentHashMap<String, Audio> audioRegions = new ConcurrentHashMap<>();

    private String data = "%%__USER__%% %%__RESOURCE__%% %%__NONCE__%%";

    private RegionManager() {

    }

    public void tick(Map<UUID, User> connectedUsers) {
        for (User user : connectedUsers.values()) {
            RegionInfo regionInfo = infoMap.get(user.getPlayerId());
            if (regionInfo == null) continue;
            List<IWrappedRegion> lastRegions = regionInfo.getLastRegions();

            Player p = Bukkit.getPlayer(user.getPlayerId());
            Set<IWrappedRegion> playerRegions = api.getRegions(p.getLocation());
            // Found highest priority regions
            List<IWrappedRegion> highestPriorityRegions = new ArrayList<>();
            int highestPriority = Integer.MIN_VALUE;

            for (IWrappedRegion region : playerRegions) {
                if (!audioRegions.containsKey(region.getId())) continue;
                int priority = region.getPriority();
                if (priority > highestPriority) {
                    highestPriority = priority;
                    highestPriorityRegions.clear();
                }
                if (priority == highestPriority) highestPriorityRegions.add(region);
            }


            HashSet<String> lastRegionsString = new HashSet<>();
            for (IWrappedRegion region : lastRegions) lastRegionsString.add(region.getId());

            HashSet<String> highestPriorityRegionsString = new HashSet<>();
            for (IWrappedRegion region : highestPriorityRegions) highestPriorityRegionsString.add(region.getId());

            for (String region : highestPriorityRegionsString) {
                if (!lastRegionsString.contains(region)) {
                    // To add
                    user.playAudio(audioRegions.get(region));
                }
            }

            for (String region : lastRegionsString) {
                if (!highestPriorityRegionsString.contains(region)) {
                    // To remove
                    user.stopAudio(audioRegions.get(region));
                }
            }

            lastRegions.clear();
            lastRegions.addAll(highestPriorityRegions);
        }
    }

    public HashMap<UUID, RegionInfo> getInfoMap() {
        return infoMap;
    }

    public void addRegion(String region, Audio audio) {
        audioRegions.put(region, audio);
    }

    public void removeRegion(String region) {
        audioRegions.remove(region);
    }

    public static RegionManager create() {
        if (api.getWorldGuardPlugin() == null || !api.getWorldGuardPlugin().isEnabled()) return null;
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

    public static WorldGuardWrapper getApi() {
        return api;
    }

}