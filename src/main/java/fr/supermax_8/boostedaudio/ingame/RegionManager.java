package fr.supermax_8.boostedaudio.ingame;

import fr.supermax_8.boostedaudio.web.Audio;
import fr.supermax_8.boostedaudio.web.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.*;

public class RegionManager {

    private static final WorldGuardWrapper api = WorldGuardWrapper.getInstance();
    private final HashMap<UUID, RegionInfo> infoMap = new HashMap<>();

    public static final HashMap<String, Audio> audioRegions = new HashMap<>();

    private RegionManager() {

    }

    public void tick(Map<UUID, User> connectedUsers) {
        for (User user : connectedUsers.values()) {
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

            List<IWrappedRegion> lastRegions = infoMap.get(user.getPlayerId()).getLastRegions();
            for (IWrappedRegion region : highestPriorityRegions) {
                if (!lastRegions.contains(region)) {
                    // To add
                    user.playAudio(audioRegions.get(region.getId()));
                }
            }

            for (IWrappedRegion region : lastRegions) {
                if (!highestPriorityRegions.contains(region)) {
                    // To remove
                    user.stopAudio(audioRegions.get(region.getId()));
                }
            }


            lastRegions.clear();
            lastRegions.addAll(highestPriorityRegions);
        }
    }


    public static RegionManager create() {
        if (api.getWorldGuardPlugin() == null || !api.getWorldGuardPlugin().isEnabled()) return null;
        return new RegionManager();
    }


    private static class RegionInfo {

        private final List<IWrappedRegion> lastRegions = new ArrayList<>();

        public List<IWrappedRegion> getLastRegions() {
            return lastRegions;
        }

    }

}