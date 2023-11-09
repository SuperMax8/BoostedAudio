package fr.supermax_8.boostedaudio.spigot.commands;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.user.User;
import fr.supermax_8.boostedaudio.core.BoostedAudioLoader;
import fr.supermax_8.boostedaudio.core.Limiter;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.gui.BoostedAudioGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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
                    int fade = Integer.parseInt(args[3]);
                    try {
                        BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(p.getUniqueId()).playAudio(link, fade);
                    } catch (Exception e) {
                    }
                }
                case "playradius" -> {
                    String link;
                    int radius;
                    Location loc;
                    int fade;
                    if (args.length == 4) {
                        link = args[1];
                        radius = Integer.parseInt(args[2]);
                        fade = Integer.parseInt(args[3]);
                        loc = ((Player) sender).getLocation();
                    } else {
                        String world = args[1];
                        int x = Integer.parseInt(args[2]);
                        int y = Integer.parseInt(args[3]);
                        int z = Integer.parseInt(args[4]);
                        link = args[5];
                        radius = Integer.parseInt(args[6]);
                        fade = Integer.parseInt(args[7]);
                        loc = new Location(Bukkit.getWorld(world), x, y, z);
                    }
                    Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, radius, radius, radius);
                    CompletableFuture.runAsync(() -> {
                        Map<UUID, User> users = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer();
                        for (Entity nearbyEntity : entities) {
                            if (!(nearbyEntity instanceof Player p)) continue;
                            User user = users.get(p.getUniqueId());
                            if (user != null) user.playAudio(link, fade);
                        }
                    });
                }
                case "stop" -> {
                    String link = args[1];
                    Player p = Bukkit.getPlayer(args[2]);
                    try {
                        BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(p.getUniqueId()).stopAudio(link);
                    } catch (Exception e) {
                    }
                }
                case "userlist" -> {
                    sender.sendMessage("§7Users on server:");
                    StringJoiner joiner = new StringJoiner("§8, ");
                    BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().values().forEach(u -> {
                        joiner.add((u.isMuted() ? "§c§l" : "§f§l") + Bukkit.getPlayer(u.getPlayerId()).getName());
                    });
                    sender.sendMessage(joiner.toString());
                }
                case "mute" -> {
                    String player = args[1];
                    Player p = Bukkit.getPlayer(player);
                    try {
                        User user = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(p.getUniqueId());
                        if (user.isMuted()) {
                            sender.sendMessage("§cPlayer is already muted");
                            return false;
                        }
                        long endTime = (long) (System.currentTimeMillis() + 1000 * 60 * Float.parseFloat(args[2]));
                        user.setMuted(true, endTime);
                        sender.sendMessage("§7Player §f§l" + p.getName() + " §7is now muted !");
                    } catch (Exception e) {
                    }
                }
                case "unmute" -> {
                    String player = args[1];
                    Player p = Bukkit.getPlayer(player);
                    try {
                        User user = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(p.getUniqueId());
                        if (!user.isMuted()) {
                            sender.sendMessage("§cPlayer is already unmuted");
                            return false;
                        }
                        user.setMuted(false, 0);
                        sender.sendMessage("§7Player §f§l" + p.getName() + " §7is now unmuted !");
                    } catch (Exception e) {
                    }
                }
                case "stopradius" -> {
                    String link;
                    int radius;
                    Location loc;
                    if (args.length == 3) {
                        link = args[1];
                        radius = Integer.parseInt(args[2]);
                        loc = ((Player) sender).getLocation();
                    } else {
                        String world = args[1];
                        int x = Integer.parseInt(args[2]);
                        int y = Integer.parseInt(args[3]);
                        int z = Integer.parseInt(args[4]);
                        link = args[5];
                        radius = Integer.parseInt(args[6]);
                        loc = new Location(Bukkit.getWorld(world), x, y, z);
                    }
                    Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, radius, radius, radius);
                    CompletableFuture.runAsync(() -> {
                        Map<UUID, User> users = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer();
                        for (Entity nearbyEntity : entities) {
                            if (!(nearbyEntity instanceof Player p)) continue;
                            User user = users.get(p.getUniqueId());
                            if (user != null) user.stopAudio(link);
                        }
                    });
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
                "§8§l[§9§lBoostedAudio§8§l] §7v§f" + BoostedAudioSpigot.getInstance().getPluginVersion() + " " + (Limiter.isPremium() ? "§6§lPremium" : "§aFree"),
                "",
                "§7/boostedaudio help §8- §7Show this help",
                //"§7/boostedaudio reload §8- §7Reload the plugin (can make the server freeze temporarily, and kick all players)",
                "§7/boostedaudio edit §8- Open edition GUI",
                "",
                "§7/boostedaudio userlist §8- List the players connected to the audio panel on the server, the players in red are muted",
                "",
                "§7/boostedaudio mute <Player> <TimeInMinutes> §8Mute a player in proximity chat",
                "§7/boostedaudio unmute <Player> §8UnMute a player in proximity chat",
                "",
                "§7/boostedaudio play <AudioLink> <Player> <Fade> §8- Play a sound for a player if he's connected",
                "§7/boostedaudio playradius <AudioLink> <Radius> <Fade> §8- Play a sound for a players if connected in radius at your location",
                "§7/boostedaudio playradius <world> <x> <y> <z> <AudioLink> <Radius> <Fade> §8- Play a sound for a players if connected in radius at a location",
                "§7/boostedaudio stop <AudioLink> <Player> §8- Stop a sound for a player if he's connected",
                "§7/boostedaudio stopradius <AudioLink> <Radius> §8- Stop a sound for a players if connected in radius",
                "§7/boostedaudio stopradius <world> <x> <y> <z> <AudioLink> <Radius> §8- Stop a sound for a players if connected in radius at a location",
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        switch (args.length) {
            case 1 -> {
                return List.of("help", "edit", "userlist", "mute", "unmute", "play", "playradius", "stop", "stopradius");
            }
            case 2 -> {
                return List.of();
            }
        }
        return null;
    }


}