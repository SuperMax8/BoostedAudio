package fr.supermax_8.boostedaudio.commands;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.BoostedAudioConfiguration;
import fr.supermax_8.boostedaudio.utils.MessageUtils;
import fr.supermax_8.boostedaudio.web.ConnectionManager;
import fr.supermax_8.boostedaudio.web.User;
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

    private static final int TOKEN_LENGTH = 8;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is for player");
            return false;
        }
        Player p = (Player) sender;
        sendConnectMessage(p);
        return false;
    }

    public static String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        random.nextBytes(tokenBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    public static void sendConnectMessage(Player p) {
        UUID playerId = p.getUniqueId();

        ConnectionManager manager = BoostedAudio.getInstance().manager;
        Map<UUID, String> tokenMap = manager.getPlayerTokens();
        if (tokenMap.containsKey(playerId)) {
            User user = manager.getUsers().get(playerId);
            if (user != null) {
                try {
                    user.getSession().close();
                } catch (Exception ex) {
                }
                BoostedAudio.debug("sendConnectMessage close() session");
            }
        }
        String token = generateToken();
        tokenMap.put(playerId, token);

        BoostedAudioConfiguration configuration = BoostedAudio.getInstance().getConfiguration();
        TextComponent text = MessageUtils.colorFormatToTextComponent(new StringBuilder(configuration.getConnectionMessage()));
        text.setColor(ChatColor.GOLD);

        String link = BoostedAudio.getInstance().getConfiguration().getClientLink() + "?token=" + token + "&playerId=" + playerId;
        text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(MessageUtils.colorFormat(new StringBuilder(configuration.getConnectionHoverMessage())).toString()).create()));

        p.spigot().sendMessage(text);
    }

}