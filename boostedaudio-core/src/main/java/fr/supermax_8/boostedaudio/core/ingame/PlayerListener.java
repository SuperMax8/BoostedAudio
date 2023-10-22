//package fr.supermax_8.boostedaudio.core.ingame;
//
//import fr.supermax_8.boostedaudio.core.BoostedAudioHost;
//import fr.supermax_8.boostedaudio.core.BoostedAudioConfiguration;
//import fr.supermax_8.boostedaudio.core.BoostedAudioLoader;
//import fr.supermax_8.boostedaudio.commands.AudioCommand;
//import fr.supermax_8.boostedaudio.core.websocket.User;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//import org.bukkit.event.player.PlayerJoinEvent;
//import org.bukkit.event.player.PlayerQuitEvent;
//import org.bukkit.scheduler.BukkitRunnable;
//
//import java.util.concurrent.CompletableFuture;
//
//public class PlayerListener implements Listener {
//
//    private String data = "%%__USER__%% %%__RESOURCE__%% %%__NONCE__%%";
//
//    @EventHandler
//    public void join(PlayerJoinEvent e) {
//        Player p = e.getPlayer();
//        BoostedAudioConfiguration config = BoostedAudioHost.getInstance().getConfiguration();
//        if (config.isSendOnConnect())
//            new BukkitRunnable() {
//                @Override
//                public void run() {
//                    AudioCommand.sendConnectMessage(p);
//                }
//            }.runTaskLaterAsynchronously(BoostedAudioLoader.getInstance(), config.getSendOnConnectDelay());
//
//        RegionManager regionManager = BoostedAudioHost.getInstance().getAudioManager().getRegionManager();
//        if (regionManager == null) return;
//        regionManager.getInfoMap().put(p.getUniqueId(), new RegionManager.RegionInfo());
//    }
//
//    @EventHandler
//    public void quit(PlayerQuitEvent e) {
//        User user = BoostedAudioHost.getInstance().getWebSocketServer().manager.getUsers().get(e.getPlayer().getUniqueId());
//        if (user != null)
//            CompletableFuture.runAsync(() -> {
//                BoostedAudioAPI.api.debug("quit close() session");
//                user.getSession().close();
//            });
//
//        RegionManager regionManager = BoostedAudioHost.getInstance().getAudioManager().getRegionManager();
//        if (regionManager == null) return;
//        Player p = e.getPlayer();
//        regionManager.getInfoMap().remove(p.getUniqueId());
//    }
//
//
//}