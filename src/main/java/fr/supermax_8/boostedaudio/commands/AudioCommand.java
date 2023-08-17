package fr.supermax_8.boostedaudio.commands;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.web.ConnectionManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AudioCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConnectionManager manager = BoostedAudio.getInstance().getWebSocketServer().manager;
        List<UUID> list = manager.getUsers().values().stream().map(user -> user.getPlayerId()).collect(Collectors.toList());
        manager.getUsers().forEach((id, user) -> {
            List<UUID> exclude = new LinkedList<>(list);
            exclude.remove(id);
            manager.setRemotePeers(id, exclude);
        });
        return false;
    }

}