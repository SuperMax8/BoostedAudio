package fr.supermax_8.boostedaudio.spigot;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.BoostedAudioConfiguration;
import fr.supermax_8.boostedaudio.spigot.commands.AudioCommand;
import fr.supermax_8.boostedaudio.spigot.utils.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void join(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        BoostedAudioConfiguration config = BoostedAudioAPI.getAPI().getConfiguration();
        Scheduler.runTaskAsync(() -> {
            if (config.isSendOnConnect() && !BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().containsKey(p.getUniqueId()))
                AudioCommand.sendConnectMessage(p);
        });
    }


}