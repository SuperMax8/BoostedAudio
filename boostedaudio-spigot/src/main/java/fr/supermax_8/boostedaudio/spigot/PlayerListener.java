package fr.supermax_8.boostedaudio.spigot;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.core.BoostedAudioConfiguration;
import fr.supermax_8.boostedaudio.core.BoostedAudioHost;
import fr.supermax_8.boostedaudio.spigot.commands.AudioCommand;
import fr.supermax_8.boostedaudio.spigot.manager.RegionManager;
import fr.supermax_8.boostedaudio.spigot.utils.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.CompletableFuture;

public class PlayerListener implements Listener {

    @EventHandler
    public void join(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        BoostedAudioConfiguration config = BoostedAudioAPI.getAPI().getConfiguration();
        Scheduler.runTaskAsync(() -> {
            if (config.isSendOnConnect() && !BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().containsKey(p.getUniqueId()))
                AudioCommand.sendConnectMessage(p);
        });

        RegionManager regionManager = BoostedAudioSpigot.getInstance().getAudioManager().getRegionManager();
        if (regionManager == null) return;
        regionManager.getInfoMap().put(p.getUniqueId(), new RegionManager.RegionInfo());
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        RegionManager regionManager = BoostedAudioSpigot.getInstance().getAudioManager().getRegionManager();
        if (regionManager == null) return;
        Player p = e.getPlayer();
        regionManager.getInfoMap().remove(p.getUniqueId());
    }


}