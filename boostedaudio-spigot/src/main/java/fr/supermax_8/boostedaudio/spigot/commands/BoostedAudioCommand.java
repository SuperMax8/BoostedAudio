package fr.supermax_8.boostedaudio.spigot.commands;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.user.User;
import fr.supermax_8.boostedaudio.core.Limiter;
import fr.supermax_8.boostedaudio.core.utils.Lang;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.gui.BoostedAudioGUI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BoostedAudioCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        try {
            if (args.length == 0) {
                sendHelp(sender);
                return false;
            }
            switch (args[0].toLowerCase()) {
                case "help" -> sendHelp(sender);
/*                case "reload":
                    sender.sendMessage("§7Plugin reload...");
                    *//*BoostedAudioHost.getInstance().reload();*//*
                    sender.sendMessage("§aPlugin reloaded !");
                    break;*/
                case "edit" -> {
                    if (!(sender instanceof Player p)) return false;
                    if (!sender.hasPermission("boostedaudio.admin")) return false;
                    new BoostedAudioGUI(p);
                }
                case "download" -> {
                    if (!sender.hasPermission("boostedaudio.admin")) return false;
                    sender.sendMessage(Lang.get("download_start"));
                    CompletableFuture.runAsync(() -> {
                        BoostedAudioSpigot.downloadAudio(args[1], audioLink -> {
                            TextComponent component = new TextComponent(Lang.get("download_end", audioLink));
                            try {
                                component.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, audioLink));
                            } catch (Throwable ex) {
                            }
                            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                                    Lang.get("click_copy_clipboard")).create()));
                            sender.spigot().sendMessage(component);
                        });
                    });
                }
                case "play" -> {
                    if (!sender.hasPermission("boostedaudio.audio")) return false;
                    String link = args[1];
                    int fade;
                    if (args.length == 4) {
                        Player p = Bukkit.getPlayer(args[2]);
                        fade = Integer.parseInt(args[3]);
                        try {
                            BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(p.getUniqueId()).playAudio(link, fade);
                        } catch (Exception ignored) {
                        }
                    } else {
                        try {
                            fade = Integer.parseInt(args[3]);
                        } catch (Exception e) {
                            fade = 500;
                        }
                        int finalFade = fade;
                        BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().forEach(((uuid, user) -> {
                            user.playAudio(link, finalFade);
                        }));
                    }
                }
                case "playradius" -> {
                    if (!sender.hasPermission("boostedaudio.audio")) return false;
                    String link;
                    int radius;
                    Location loc;
                    int fade;
                    if (args.length == 4) {
                        link = args[1];
                        radius = Integer.parseInt(args[2]);
                        fade = Integer.parseInt(args[3]);
                        if (sender instanceof Player p)
                            loc = p.getLocation();
                        else if (sender instanceof BlockCommandSender blockCommandSender)
                            loc = blockCommandSender.getBlock().getLocation();
                        else loc = null;
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
                    if (!sender.hasPermission("boostedaudio.audio")) return false;
                    String link = args[1];
                    if (args.length == 3) {
                        Player p = Bukkit.getPlayer(args[2]);
                        try {
                            BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(p.getUniqueId()).stopAudio(link);
                        } catch (Exception ignored) {
                        }
                    } else {
                        BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().forEach(((uuid, user) -> {
                            user.stopAudio(link);
                        }));
                    }
                }
                case "stopradius" -> {
                    if (!sender.hasPermission("boostedaudio.audio")) return false;
                    String link;
                    int radius;
                    Location loc;
                    if (args.length == 3) {
                        link = args[1];
                        radius = Integer.parseInt(args[2]);
                        if (sender instanceof Player p)
                            loc = p.getLocation();
                        else if (sender instanceof BlockCommandSender blockCommandSender)
                            loc = blockCommandSender.getBlock().getLocation();
                        else loc = null;
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
                case "userlist" -> {
                    if (!sender.hasPermission("boostedaudio.userlist")) return false;
                    sender.sendMessage(Lang.get("users_on_server"));
                    StringJoiner joiner = new StringJoiner("§8, ");
                    BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().values().forEach(u -> {
                        joiner.add((u.isMuted() ? "§c§l" : "§f§l") + Bukkit.getPlayer(u.getPlayerId()).getName());
                    });
                    sender.sendMessage(joiner.toString());
                }
                case "mute" -> {
                    if (!sender.hasPermission("boostedaudio.mute")) return false;
                    String player = args[1];
                    Player p = Bukkit.getPlayer(player);
                    try {
                        User user = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(p.getUniqueId());
                        if (user.isMuted()) {
                            sender.sendMessage(Lang.get("player_already_muted"));
                            return false;
                        }
                        long endTime = (long) (System.currentTimeMillis() + 1000 * 60 * Float.parseFloat(args[2]));
                        user.setMuted(true, endTime);
                        sender.sendMessage(Lang.get("player_now_muted", p.getName()));
                    } catch (Exception ignored) {
                    }
                }
                case "unmute" -> {
                    if (!sender.hasPermission("boostedaudio.mute")) return false;
                    String player = args[1];
                    Player p = Bukkit.getPlayer(player);
                    try {
                        User user = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(p.getUniqueId());
                        if (!user.isMuted()) {
                            sender.sendMessage(Lang.get("player_already_unmuted"));
                            return false;
                        }
                        user.setMuted(false, 0);
                        sender.sendMessage(Lang.get("player_now_unmuted", p.getName()));
                    } catch (Exception ignored) {
                    }
                }
                /*case "test" -> {
                    String link = args[1];
                    Player p = (Player) sender;
                    User user = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(p.getUniqueId());
                    Audio audio = user.playAudio(link,
                            new Audio.AudioSpatialInfo(InternalUtils.bukkitLocationToSerializableLoc(p.getLocation()), 15),
                            350
                    );
                    double distance = 10;
                    AtomicInteger angle = new AtomicInteger();
                    AtomicInteger count = new AtomicInteger();
                    Bukkit.getScheduler().runTaskTimerAsynchronously(BoostedAudioSpigot.getInstance(), t -> {
                        if (count.get() == 20 * 20) {
                            t.cancel();
                            audio.stop();
                            p.sendMessage("End");
                            return;
                        }
                        double radians = Math.toRadians(angle.get());
                        Location currentLocation = p.getLocation();
                        double x = currentLocation.getX() + distance * Math.cos(radians);
                        double z = currentLocation.getZ() + distance * Math.sin(radians);
                        Location newLocation = new Location(currentLocation.getWorld(), x, currentLocation.getY(), z);
                        audio.updateLocation(InternalUtils.bukkitLocationToSerializableLoc(newLocation));
                        if (angle.get() >= 360) angle.set(0);
                        else {
                            angle.addAndGet(5);
                        }
                        Bukkit.getScheduler().runTask(BoostedAudioSpigot.getInstance(), () -> {
                            newLocation.getWorld().spawnParticle(Particle.FLAME, newLocation, 10);
                        });
                        count.incrementAndGet();
                    }, 0, 0);
                }*/
                default -> sendHelp(sender);
            }
        } catch (Exception e) {
            sendHelp(sender);
            if (BoostedAudioAPI.getAPI().getConfiguration().isDebugMode()) {
                BoostedAudioAPI.getAPI().debug("DEBUG MESSAGE");
                e.printStackTrace();
            }
        }
        return false;
    }


    private void sendHelp(CommandSender sender) {
        sender.sendMessage(new String[]{
                "§8§l[§9§lBoostedAudio§8§l] §7v§f" + BoostedAudioSpigot.getInstance().getPluginVersion() + " " + (Limiter.isPremium() ? "§6§lPremium" : "§aFree"),
                "",
                "§7/boostedaudio help §8- " + Lang.get("command_help"),
                //"§7/boostedaudio reload §8- §7Reload the plugin (can make the server freeze temporarily, and kick all players)",
                "§7/boostedaudio edit §8-" + Lang.get("command_open_edition"),
                "",
                "§7/boostedaudio userlist §8- " + Lang.get("command_userlist"),
                "",
                "§7/boostedaudio mute " + Lang.get("command_mute"),
                "§7/boostedaudio unmute " + Lang.get("command_unmute"),
                "",
                "§7/boostedaudio play " + Lang.get("command_play1"),
                "§7/boostedaudio play " + Lang.get("command_play2"),
                "§7/boostedaudio playradius " + Lang.get("command_playradius1"),
                "§7/boostedaudio playradius " + Lang.get("command_playradius2"),
                "§7/boostedaudio stop " + Lang.get("command_stop1"),
                "§7/boostedaudio stop " + Lang.get("command_stop2"),
                "§7/boostedaudio stopradius " + Lang.get("command_stopradius1"),
                "§7/boostedaudio stopradius " + Lang.get("command_stopradius2"),
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> completions;
        String arg;
        switch (args.length) {
            case 1 -> {
                arg = args[0];
                completions = Arrays.asList("help", "edit", "userlist", "mute", "unmute", "play", "playradius", "stop", "stopradius");
            }
            case 2 -> {
                arg = args[1];
                switch (args[0]) {
                    case "mute", "unmute" ->
                            completions = Bukkit.getOnlinePlayers().stream().map(p -> p.getName()).toList();
                    default -> completions = new ArrayList<>();
                }
            }
            default -> {
                arg = "";
                completions = new ArrayList<>();
            }
        }

        List<String> finalList = new ArrayList<>();
        StringUtil.copyPartialMatches(arg, completions, finalList);

        Collections.sort(finalList);
        return finalList;
    }


}