package fr.supermax_8.boostedaudio.commands;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.utils.HashBiMap;
import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.ConnectionManager;
import fr.supermax_8.boostedaudio.web.User;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AudioCommand implements CommandExecutor {

    private static final int TOKEN_LENGTH = 32;


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is for player");
            return false;
        }
        Player p = (Player) sender;
        UUID playerId = p.getUniqueId();

        ConnectionManager manager = BoostedAudio.getInstance().getWebSocketServer().manager;
        HashBiMap<UUID, String> tokenMap = manager.getPlayerTokens();
        if (tokenMap.containsKey(playerId)) {
            User user = manager.getUsers().get(playerId);
            if (user != null) user.getSession().close();
        }
        String token = generateToken();
        tokenMap.put(playerId, token);

        TextComponent text = new TextComponent("Join the audio client by clicking here!");
        text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ""));

        /*ConnectionManager manager = BoostedAudio.getInstance().getWebSocketServer().manager;
        List<UUID> list = manager.getUsers().values().stream().map(User::getPlayerId).collect(Collectors.toList());
        manager.getUsers().forEach((id, user) -> {
            List<UUID> exclude = new LinkedList<>(list);
            exclude.remove(id);
            manager.setRemotePeers(id, exclude);
        });*/
        return false;
    }

    public static String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        random.nextBytes(tokenBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

}