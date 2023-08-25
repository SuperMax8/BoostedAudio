package fr.supermax_8.boostedaudio.commands;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.utils.SerializableLocation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BoostedAudioCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("boostedaudio.admin")) return false;
        Player p = (Player) sender;
        BoostedAudio.getInstance().getWebSocketServer().manager.getUsers().values().forEach(u -> {
            u.playAudio(args[0], new SerializableLocation(p.getLocation()), 300, 300, true);

        });
        sender.sendMessage("Audio send");
        return false;
    }


}