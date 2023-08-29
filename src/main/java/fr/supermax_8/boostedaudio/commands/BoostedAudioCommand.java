package fr.supermax_8.boostedaudio.commands;

import fr.supermax_8.boostedaudio.gui.BoostedAudioGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BoostedAudioCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("boostedaudio.admin")) return false;
        if (!(sender instanceof Player)) return false;
        Player p = (Player) sender;
        new BoostedAudioGUI(p);
        return false;
    }


}