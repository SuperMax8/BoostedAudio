package fr.supermax_8.boostedaudio.ingame;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.web.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.CompletableFuture;

public class PlayerListener implements Listener {


    @EventHandler
    public void quit(PlayerQuitEvent e) {
        User user = BoostedAudio.getInstance().getWebSocketServer().manager.getUsers().get(e.getPlayer().getUniqueId());
        if (user != null)
            CompletableFuture.runAsync(() -> user.getSession().close());
    }


}