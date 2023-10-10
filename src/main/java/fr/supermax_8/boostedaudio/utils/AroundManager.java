package fr.supermax_8.boostedaudio.utils;

import fr.supermax_8.boostedaudio.BoostedAudioLoader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class AroundManager extends BukkitRunnable {

    private final ConcurrentHashMap<ChunkCoord, ConcurrentHashMap<Location, Around>> arounds = new ConcurrentHashMap<>();

    private ConcurrentHashMap<UUID, ConcurrentHashMap<Location, Around>> whosAround = new ConcurrentHashMap<>();
    private double biggestRadius = 0;

    public AroundManager() {
        runTaskTimerAsynchronously(BoostedAudioLoader.getInstance(), 0, 0);
    }

    @Override
    public void run() {
        int lengthCheck = (int) Math.max(1, Math.ceil(biggestRadius / 16));
        ConcurrentHashMap<UUID, ConcurrentHashMap<Location, Around>> whosAround = new ConcurrentHashMap<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            Location loc = p.getLocation();
            ChunkCoord playerChunkCoord = new ChunkCoord(loc);
            UUID pId = p.getUniqueId();
            ConcurrentHashMap<Location, Around> oldPlayerArounds = this.whosAround.get(pId);
            ConcurrentHashMap<Location, Around> newPlayerArounds = new ConcurrentHashMap<>();

            for (int x = playerChunkCoord.x - lengthCheck; x <= playerChunkCoord.x + lengthCheck; x++) {
                for (int z = playerChunkCoord.z - lengthCheck; z <= playerChunkCoord.z + lengthCheck; z++) {
                    ChunkCoord chunkCoord = playerChunkCoord.clone().set(x, z);
                    ConcurrentHashMap<Location, Around> locations = arounds.get(chunkCoord);
                    if (locations == null) continue;

                    for (Map.Entry<Location, Around> en : locations.entrySet()) {
                        Location location = en.getKey();
                        Around around = en.getValue();
                        if (location.distanceSquared(loc) > around.radius * around.radius) {
                            if (oldPlayerArounds != null && oldPlayerArounds.containsKey(location)) {
                                Consumer<Player> onLeave = around.onLeave;
                                if (onLeave != null) onLeave.accept(p);
                            }
                        } else {
                            if (!around.detection.test(p)) continue;
                            newPlayerArounds.put(location, around);
                            if (oldPlayerArounds == null || !oldPlayerArounds.containsKey(location)) {
                                Consumer<Player> onEnter = around.onEnter;
                                if (onEnter != null) onEnter.accept(p);
                            }
                        }
                    }
                }
            }
            whosAround.put(pId, newPlayerArounds);
        }
        this.whosAround = whosAround;
    }

    public void addAround(Location location, double radius, Consumer<Player> onEnter, Consumer<Player> onLeave, Predicate<Player> detection) {
        if (radius > biggestRadius) biggestRadius = radius;
        ChunkCoord chunkCoord = new ChunkCoord(location.getChunk());
        ConcurrentHashMap<Location, Around> locations = arounds.get(chunkCoord);
        if (locations == null) {
            locations = new ConcurrentHashMap<>();
            arounds.put(chunkCoord, locations);
        }
        locations.put(location, new Around(onEnter, onLeave, radius, detection));
    }

    public void removeAround(Location location) {
        ChunkCoord chunkCoord = new ChunkCoord(location.getChunk());
        ConcurrentHashMap<Location, Around> locations = arounds.get(chunkCoord);
        if (locations == null) return;
        locations.remove(location);
    }

    public ConcurrentHashMap<ChunkCoord, ConcurrentHashMap<Location, Around>> getArounds() {
        return arounds;
    }

    public ConcurrentHashMap<UUID, ConcurrentHashMap<Location, Around>> getWhosAround() {
        return whosAround;
    }

    public static class Around {

        private final Consumer<Player> onEnter;

        private final Consumer<Player> onLeave;
        private final Predicate<Player> detection;

        private final double radius;

        public Around(Consumer<Player> onEnter, Consumer<Player> onLeave, double radius, Predicate<Player> detection) {
            this.onEnter = onEnter;
            this.onLeave = onLeave;
            this.radius = radius;
            this.detection = detection;
        }

        public Consumer<Player> getOnEnter() {
            return onEnter;
        }

        public Consumer<Player> getOnLeave() {
            return onLeave;
        }

        public double getRadius() {
            return radius;
        }

        public Predicate<Player> getDetection() {
            return detection;
        }

    }

}