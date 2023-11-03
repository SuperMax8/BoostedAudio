package fr.supermax_8.boostedaudio.bungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class AudioCommandBungee extends Command {


    public AudioCommandBungee(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer p) sendConnectMessage(p, p.getServer().getInfo().getName());
    }


    public static void sendConnectMessage(ProxiedPlayer player, String servername) {
        String link = BoostedAudioBungee.getInstance().getConfiguration().getClientLink()
                + "?t="
                + BoostedAudioBungee.getInstance().getHost().getWebSocketServer().manager
                .generateConnectionToken(player.getUniqueId());
        BoostedAudioBungee.sendPluginMessage(
                servername,
                "audiotoken",
                player.getUniqueId().toString() + ";" + link
        );
    }

}