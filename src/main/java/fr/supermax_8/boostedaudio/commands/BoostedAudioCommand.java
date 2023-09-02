package fr.supermax_8.boostedaudio.commands;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.gui.BoostedAudioGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BoostedAudioCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("boostedaudio.admin")) return false;
        try {
            switch (args[0].toLowerCase()) {
                case "help":
                    sendHelp(sender);
                    break;
                case "reload":
                    sender.sendMessage("§7Plugin reload...");
                    BoostedAudio.getInstance().reload();
                    sender.sendMessage("§aPlugin reloaded !");
                    break;
                case "edit":
                    if (!(sender instanceof Player)) return false;
                    Player p = (Player) sender;
                    new BoostedAudioGUI(p);
                    break;
                default:
                    sendHelp(sender);
                    break;
            }
        } catch (Exception e) {
            sendHelp(sender);
        }
        return false;
    }


    private void sendHelp(CommandSender sender) {
        sender.sendMessage(new String[]{
                "§8§l[§9§lBoostedAudio§8§l] §7v§f" + BoostedAudio.VERSION + " " + (BoostedAudio.getInstance().isPremium() ? "§6§lPremium" : "§aFree"),
                "",
                "§7/boostedaudio help §8- §7Show this help",
                "§7/boostedaudio reload §8- §7Reload the plugin",
                "§7/boostedaudio edit §8- Open edition GUI",
        });
    }

}