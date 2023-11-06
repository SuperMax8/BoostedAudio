package fr.supermax_8.boostedaudio.spigot.commands;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.BoostedAudioLoader;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.gui.BoostedAudioGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class BoostedAudioCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("boostedaudio.admin")) return false;
        try {
            switch (args[0].toLowerCase()) {
                case "help" -> sendHelp(sender);

/*                case "reload":
                    sender.sendMessage("§7Plugin reload...");
                    *//*BoostedAudioHost.getInstance().reload();*//*
                    sender.sendMessage("§aPlugin reloaded !");
                    break;*/
                case "edit" -> {
                    if (!(sender instanceof Player p)) return false;
                    new BoostedAudioGUI(p);
                }
                case "play" -> {
                    String link = args[1];
                    Player p = Bukkit.getPlayer(args[2]);
                    try {
                        BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(p.getUniqueId()).playAudio(link, 300);
                    } catch (Exception e) {
                    }
                }
                case "stop" -> {
                    String link = args[1];
                    Player p = Bukkit.getPlayer(args[2]);
                    try {
                        BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(p.getUniqueId()).stopAudio(link);
                    } catch (Exception e) {
                    }
                }
                default -> sendHelp(sender);
            }
        } catch (Exception e) {
            sendHelp(sender);
        }
        return false;
    }


    private void sendHelp(CommandSender sender) {
        sender.sendMessage(new String[]{
                "§8§l[§9§lBoostedAudio§8§l] §7v§f" + BoostedAudioSpigot.getInstance().getPluginVersion() + " " + (BoostedAudioLoader.isPremium() ? "§6§lPremium" : "§aFree"),
                "",
                "§7/boostedaudio help §8- §7Show this help",
                //"§7/boostedaudio reload §8- §7Reload the plugin (can make the server freeze temporarily, and kick all players)",
                "§7/boostedaudio edit §8- Open edition GUI",
                "§7/boostedaudio play <Link> <Player> §8- Play a sound for a player if he's connected",
                "§7/boostedaudio stop <Link> <Player> §8- Stop a sound for a player if he's connected",
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length == 1) return List.of("help", "reload", "edit");
        return null;
    }


}