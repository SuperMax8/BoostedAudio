package fr.supermax_8.boostedaudio.ingame;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.BoostedAudioConfiguration;
import fr.supermax_8.boostedaudio.BoostedAudioLoader;
import fr.supermax_8.boostedaudio.commands.AudioCommand;
import fr.supermax_8.boostedaudio.web.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CompletableFuture;

public class PlayerListener implements Listener {

    private final BoostedAudioConfiguration config = BoostedAudio.getInstance().getConfiguration();

    @EventHandler
    public void join(PlayerJoinEvent e) {
        RegionManager regionManager = BoostedAudio.getInstance().getAudioManager().getRegionManager();
        if (regionManager == null) return;
        Player p = e.getPlayer();
        regionManager.getInfoMap().put(p.getUniqueId(), new RegionManager.RegionInfo());
        if (config.isSendOnConnect()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    AudioCommand.sendConnectMessage(p);
                }
            }.runTaskLaterAsynchronously(BoostedAudioLoader.getInstance(), config.getSendOnConnectDelay());
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        User user = BoostedAudio.getInstance().getWebSocketServer().manager.getUsers().get(e.getPlayer().getUniqueId());
        if (user != null)
            CompletableFuture.runAsync(() -> user.getSession().close());

        RegionManager regionManager = BoostedAudio.getInstance().getAudioManager().getRegionManager();
        if (regionManager == null) return;
        Player p = e.getPlayer();
        regionManager.getInfoMap().remove(p.getUniqueId());
    }


}