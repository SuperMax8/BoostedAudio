package fr.supermax_8.boostedaudio.spigot.commands;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.BoostedAudioConfiguration;
import fr.supermax_8.boostedaudio.core.BoostedAudioHost;
import fr.supermax_8.boostedaudio.core.utils.MessageUtils;
import fr.supermax_8.boostedaudio.core.websocket.ConnectionManager;
import fr.supermax_8.boostedaudio.core.websocket.User;
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

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public class AudioCommand implements CommandExecutor {

    private static int TOKEN_LENGTH = 3;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("This command is for players");
            return false;
        }
        sendConnectMessage(p);
        return false;
    }

    public static String generateConnectionToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        random.nextBytes(tokenBytes);

        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        if (BoostedAudioHost.getInstance().getWebSocketServer().manager.getPlayerTokens().values().contains(token)) {
            TOKEN_LENGTH++;
            return generateConnectionToken();
        }
        return token;
    }

    public static void sendConnectMessage(Player p) {
        UUID playerId = p.getUniqueId();

        ConnectionManager manager = BoostedAudioHost.getInstance().getWebSocketServer().manager;
        Map<UUID, String> tokenMap = manager.getPlayerTokens();
        if (tokenMap.containsKey(playerId)) {
            User user = manager.getUsers().get(playerId);
            if (user != null) {
                user.getSession().close();
                BoostedAudioAPI.api.debug("sendConnectMessage close() session");
            }
        }
        String token = generateConnectionToken();
        tokenMap.put(playerId, token);

        BoostedAudioConfiguration configuration = BoostedAudioAPI.api.getConfiguration();

        String link = configuration.getClientLink() + "?t=" + token;
        TextComponent text = MessageUtils.colorFormatToTextComponent(new StringBuilder(configuration.getConnectionMessage().replace("{link}", link)));
        text.setColor(ChatColor.GOLD);

        text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(MessageUtils.colorFormat(new StringBuilder(configuration.getConnectionHoverMessage())).toString()).create()));

        p.spigot().sendMessage(text);
    }


}