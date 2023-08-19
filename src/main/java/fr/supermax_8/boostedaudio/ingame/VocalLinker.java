package fr.supermax_8.boostedaudio.ingame;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.web.ConnectionManager;
import fr.supermax_8.boostedaudio.web.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class VocalLinker extends BukkitRunnable {

    private final ConnectionManager manager = BoostedAudio.getInstance().getWebSocketServer().manager;
    private final double maxDistance = BoostedAudio.getInstance().getConfiguration().getMaxVoiceDistance();


    @Override
    public void run() {
        Map<UUID, User> users = manager.getUsers();
        for (User user : users.values()) {
            Player player = Bukkit.getPlayer(user.getPlayerId());
            if (player == null) {
                user.getSession().close();
                continue;
            }

            setPeers(player, user);
        }
    }

    private void setPeers(Player player, User user) {
        Bukkit.getScheduler().runTask(BoostedAudio.getInstance(), () -> {
            List<UUID> peers = player.getNearbyEntities(maxDistance, maxDistance, maxDistance).stream()
                    .filter(e -> e.getType() == EntityType.PLAYER)
                    .map(Entity::getUniqueId).collect(Collectors.toList());
            CompletableFuture.runAsync(() -> manager.setRemotePeers(user, peers));
        });
    }


}