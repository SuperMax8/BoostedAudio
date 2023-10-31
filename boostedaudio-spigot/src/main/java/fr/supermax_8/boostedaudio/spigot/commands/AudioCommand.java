package fr.supermax_8.boostedaudio.spigot.commands;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.BoostedAudioConfiguration;
import fr.supermax_8.boostedaudio.core.BoostedAudioHost;
import fr.supermax_8.boostedaudio.core.utils.MessageUtils;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AudioCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("This command is for players");
            return false;
        }
        sendConnectMessage(p);
        return false;
    }

    public static void sendConnectMessage(Player p) {
        if (BoostedAudioAPI.api.getConfiguration().isBungeecoord()) {
            BoostedAudioSpigot.sendPluginMessage("audiotoken", p.getUniqueId().toString());
        } else {
            UUID playerId = p.getUniqueId();
            String token = BoostedAudioHost.getInstance().getWebSocketServer().manager.generateConnectionToken(playerId);
            sendConnectMessage(p, token);
        }
    }

    public static void sendConnectMessage(Player p, String token) {
        BoostedAudioConfiguration configuration = BoostedAudioAPI.api.getConfiguration();

        String link = configuration.getClientLink() + "?t=" + token;
        TextComponent text = MessageUtils.colorFormatToTextComponent(new StringBuilder(configuration.getConnectionMessage().replace("{link}", link)));
        text.setColor(ChatColor.GOLD);

        text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(MessageUtils.colorFormat(new StringBuilder(configuration.getConnectionHoverMessage())).toString()).create()));

        p.spigot().sendMessage(text);
    }


}