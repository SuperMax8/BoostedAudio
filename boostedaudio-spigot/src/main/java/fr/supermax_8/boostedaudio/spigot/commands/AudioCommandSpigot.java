package fr.supermax_8.boostedaudio.spigot.commands;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.BoostedAudioConfiguration;
import fr.supermax_8.boostedaudio.core.BoostedAudioHost;
import fr.supermax_8.boostedaudio.core.utils.MessageUtils;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.utils.XMaterial;
import fr.supermax_8.boostedaudio.spigot.utils.qrcode.QrCodeGenerator;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;

import java.util.UUID;

public class AudioCommandSpigot {

    @Command({"audio", "music"})
    public boolean onCommand(Player p) {
        sendConnectMessage(p);
        return false;
    }

    public static void sendConnectMessage(Player p) {
        BoostedAudioSpigot.getInstance().getScheduler().runAsync(t -> {
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

                MessageUtils.applyRecursilvlyOnTextComponent(text, t -> {
                    t.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
                    t.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(MessageUtils.colorFormat(new StringBuilder(config.getConnectionHoverMessage())).toString()).create()));
                });
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