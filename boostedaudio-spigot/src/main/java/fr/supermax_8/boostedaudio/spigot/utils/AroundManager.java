package fr.supermax_8.boostedaudio.spigot.utils;

import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/*1.2*/
public class AroundManager implements Runnable {

    @Getter
    private final ConcurrentHashMap<ChunkCoord, ConcurrentHashMap<Location, Around>> arounds = new ConcurrentHashMap<>();

    @Getter
    private ConcurrentHashMap<UUID, ConcurrentHashMap<Location, Around>> whosAround = new ConcurrentHashMap<>();
    private double biggestRadius = 0;

    public AroundManager() {
    }

    @Override
    public void run() {
        int lengthCheck = (int) Math.max(1, Math.ceil(biggestRadius / 16));
        ConcurrentHashMap<UUID, ConcurrentHashMap<Location, Around>> whosAround = new ConcurrentHashMap<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p == null) continue;
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
        BoostedAudioSpigot.getInstance().getScheduler().runAtLocation(location, t -> {
            ChunkCoord chunkCoord = new ChunkCoord(location.getChunk());
            ConcurrentHashMap<Location, Around> locations = arounds.get(chunkCoord);
            if (locations == null) {
                locations = new ConcurrentHashMap<>();
                arounds.put(chunkCoord, locations);
            }
            locations.put(location, new Around(onEnter, onLeave, radius, detection));
        });
    }

    public void removeAround(Location location) {
        BoostedAudioSpigot.getInstance().getScheduler().runAtLocation(location, t -> {
            ChunkCoord chunkCoord = new ChunkCoord(location.getChunk());
            ConcurrentHashMap<Location, Around> locations = arounds.get(chunkCoord);
            if (locations == null) return;
            locations.remove(location);
        });
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
