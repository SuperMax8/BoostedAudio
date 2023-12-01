package fr.supermax_8.boostedaudio.velocity;

import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import fr.supermax_8.boostedaudio.core.serverpacket.ServerUser;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;

import java.util.concurrent.CompletableFuture;

public class AudioCommandVelocity implements RawCommand {

    @Override
    public void execute(Invocation invocation) {
        if (invocation.source() instanceof Player player) sendConnectMessage(player, player.getCurrentServer().get().getServer().getServerInfo().getName());
    }


    public static void sendConnectMessage(Player player, String servername) {
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

            String link = BoostedAudioVelocity.getInstance().getConfiguration().getClientLink()
                    + "?t="
                    + BoostedAudioVelocity.getInstance().getHost().getWebSocketServer().manager
                    .generateConnectionToken(player.getUniqueId());
            BoostedAudioVelocity.sendServerPacket(
                    serverUser.getServerId(),
                    "audiotoken",
                    player.getUniqueId().toString() + ";" + link
            );
        });
    }

}