package fr.supermax_8.boostedaudio.spigot.commands;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.BoostedAudioConfiguration;
import fr.supermax_8.boostedaudio.core.BoostedAudioHost;
import fr.supermax_8.boostedaudio.core.utils.MessageUtils;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.utils.XMaterial;
import fr.supermax_8.boostedaudio.spigot.utils.qrcode.QrCodeGenerator;
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
import java.util.concurrent.CompletableFuture;

public class AudioCommandSpigot implements CommandExecutor {

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
        CompletableFuture.runAsync(() -> {
            BoostedAudioConfiguration config = BoostedAudioAPI.api.getConfiguration();
            if (config.isDiffuser()) {
                BoostedAudioAPI.getAPI().debug("Sending audioTokenRequest for " + p.getName());
                BoostedAudioSpigot.sendServerPacket("audiotoken", p.getUniqueId().toString());
            } else {
                BoostedAudioAPI.getAPI().debug("Host audioTokenRequest for " + p.getName());
                UUID playerId = p.getUniqueId();
                String token = BoostedAudioHost.getInstance().getWebSocketServer().manager.generateConnectionToken(playerId);
                if (token == null) return;
                String link = config.getClientLink()
                        + "?t="
                        + token;
                sendConnectMessage(p, link);
            }
        });
    }

    public static void sendConnectMessage(Player p, String link) {
        try {
            if (AudioQRcodeCommand.requestingQRcode.contains(p.getUniqueId())) {
                QrCodeGenerator.sendMap(link, p);
                AudioQRcodeCommand.requestingQRcode.remove(p.getUniqueId());
                return;
            }
            BoostedAudioConfiguration config = BoostedAudioAPI.api.getConfiguration();

            for (String line : config.getConnectionMessage()) {
                String textString = line.replace("{link}", link);
                TextComponent text = XMaterial.supports(16) ? MessageUtils.colorFormatToTextComponent(new StringBuilder(textString)) : new TextComponent(textString);

                text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
                text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(MessageUtils.colorFormat(new StringBuilder(config.getConnectionHoverMessage())).toString()).create()));
                p.spigot().sendMessage(text);
            }

            if (BoostedAudioAPI.api.getConfiguration().isDebugMode())
                BoostedAudioAPI.getAPI().debug("Sending connection message to " + p.getName() + " : " + link);
            if (config.isSendQRcodeOnConnect()) QrCodeGenerator.sendMap(link, p);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}