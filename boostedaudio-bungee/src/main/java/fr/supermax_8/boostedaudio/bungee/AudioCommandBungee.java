package fr.supermax_8.boostedaudio.bungee;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.serverpacket.ServerUser;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.concurrent.CompletableFuture;

public class AudioCommandBungee extends Command {

    private static String aaaaaaadqs = "%%__USER__%% %%__RESOURCE__%% %%__NONCE__%% %%__USER__%% %%__RESOURCE__%% %%__NONCE__%%";

    public AudioCommandBungee(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer p) sendConnectMessage(p, p.getServer().getInfo().getName());
    }


    public static void sendConnectMessage(ProxiedPlayer player, String servername) {
        try {
            CompletableFuture.runAsync(() -> {
                ServerUser serverUser;
                try {
                    serverUser = AudioWebSocketServer.getInstance().manager.getServer(servername);
                } catch (Exception e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    serverUser = AudioWebSocketServer.getInstance().manager.getServer(servername);
                }

                String link = BoostedAudioBungee.getInstance().getConfiguration().getClientLink()
                        + "?t="
                        + BoostedAudioBungee.getInstance().getHost().getWebSocketServer().manager
                        .generateConnectionToken(player.getUniqueId());
                BoostedAudioBungee.sendServerPacket(
                        serverUser.getServerId(),
                        "audiotoken",
                        player.getUniqueId().toString() + ";" + link
                );
            });
        } catch (Throwable e) {
            if (BoostedAudioAPI.getAPI().getConfiguration().isDebugMode()) {
                BoostedAudioAPI.getAPI().debug("Failed to send audioToken, server is certainly not yet auth to the velocity (In normal case, this error is normal and can be skipped)");
                e.printStackTrace();
            }
        }
    }

}