package fr.supermax_8.boostedaudio.ingame;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.web.ConnectionManager;
import fr.supermax_8.boostedaudio.web.User;
import fr.supermax_8.boostedaudio.web.packets.UpdateVocalPositionsPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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
            List<Player> players = new LinkedList<>();
            
            // Get Nearby Players
            for (Entity entity : player.getNearbyEntities(maxDistance, maxDistance, maxDistance)) {
                if (entity.getType() != EntityType.PLAYER) continue;
                players.add((Player) entity);
            }

            List<UUID> peers = new LinkedList<>();
            for (Player p : players) peers.add(p.getUniqueId());

            CompletableFuture.runAsync(() -> {
                // Set new Peers
                manager.setRemotePeers(user, peers);

                // Send update position packet, for spatialization
                HashMap<UUID, UpdateVocalPositionsPacket.Location> peersLocs = new HashMap<>();
                for (Player p : players) peersLocs.put(p.getUniqueId(), new UpdateVocalPositionsPacket.Location(p.getLocation()));

                UpdateVocalPositionsPacket updatePacket = new UpdateVocalPositionsPacket(
                        new UpdateVocalPositionsPacket.Location(player.getLocation()),
                        peersLocs
                );
                user.send(updatePacket);
            });
        });
    }


}