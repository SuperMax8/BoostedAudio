package fr.supermax_8.boostedaudio.spigot;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.core.BoostedAudioConfiguration;
import fr.supermax_8.boostedaudio.spigot.commands.AudioCommandSpigot;
import fr.supermax_8.boostedaudio.spigot.manager.RegionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void join(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        BoostedAudioConfiguration config = BoostedAudioAPI.getAPI().getConfiguration();
        if (config.isSendOnConnect()) BoostedAudioSpigot.getInstance().getScheduler().runLaterAsync(t -> {
            if (!BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().containsKey(p.getUniqueId()))
                AudioCommandSpigot.sendConnectMessage(p);
        }, BoostedAudioAPI.getAPI().getConfiguration().getSendOnConnectDelay());
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        BoostedAudioSpigot.getInstance().getScheduler().runAsync(task -> {
            RegionManager regionManager = BoostedAudioSpigot.getInstance().getAudioManager().getRegionManager();
            if (regionManager != null) {
                regionManager.getInfoMap().remove(p.getUniqueId());
            }
            BoostedAudioConfiguration config = BoostedAudioAPI.getAPI().getConfiguration();
            if (!config.isDiffuser()) {
                User user = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(p.getUniqueId());
                if (user != null) user.close();
            }
        });
    }


}