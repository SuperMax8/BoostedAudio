package fr.supermax_8.boostedaudio.spigot.commands;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.core.Limiter;
import fr.supermax_8.boostedaudio.core.utils.Lang;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.gui.BoostedAudioGUI;
import fr.supermax_8.boostedaudio.spigot.manager.HologramManager;
import fr.supermax_8.boostedaudio.core.utils.FFmpegUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandPermission("boostedaudio.command")
@Command({"boostedaudio", "ba"})
public class BoostedAudioCommand {

    @CommandPlaceholder
    public void helpplaceholder(CommandSender sender) {
        sendHelp(sender);
    }

    @Subcommand("help")
    public void help(CommandSender sender) {
        sendHelp(sender);
    }

    @CommandPermission("boostedaudio.admin")
    @Subcommand("edit")
    public void edit(Player p) {
        new BoostedAudioGUI(p);
    }

    @CommandPermission("boostedaudio.admin")
    @Subcommand("showall")
    public void showall(Player p) {
        if (!BoostedAudioSpigot.ishologramInstalled()) {
            p.sendMessage(Lang.get("hologram_error"));
            return;
        }
        HologramManager hm = BoostedAudioSpigot.getInstance().getAudioManager().getSpeakerManager().getHologramManager();
        if (!hm.getPlayerList().contains(p)) {
            hm.getPlayerList().add(p);
            hm.getHolos().values().forEach(holo -> holo.show(p));
            p.sendMessage(Lang.get("show_all"));
        } else {
            hm.getPlayerList().remove(p);
            hm.getHolos().values().forEach(holo -> holo.hide(p));
            p.sendMessage(Lang.get("no_longer_show_all"));
        }
    }

    @CommandPermission("boostedaudio.admin")
    @Subcommand("download")
    public void download(CommandSender sender, String link) {
        sender.sendMessage(Lang.get("download_start"));
        CompletableFuture.runAsync(() -> {
            BoostedAudioSpigot.downloadAudio(link, audioLink -> {
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

    @CommandPermission("boostedaudio.admin")
    @Subcommand("linkduration")
    public void linkduration(CommandSender sender, String link) {
        CompletableFuture.runAsync(() -> {
            long t = System.currentTimeMillis();
            try {
                double d = FFmpegUtils.getAudioDuration(link);
                long t2 = System.currentTimeMillis();
                sender.sendMessage("Duration : " + d + " get in " + (t2 - t));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @CommandPermission("boostedaudio.audio")
    @Subcommand("play")
    public void play(CommandSender p, String link, Player listener, int fade) {
        BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(listener.getUniqueId()).playAudio(link, fade);
    }

    @CommandPermission("boostedaudio.audio")
    @Subcommand("play")
    public void playAll(CommandSender p, String link) {
        playAll(p, link, 500);
    }

    @CommandPermission("boostedaudio.audio")
    @Subcommand("play")
    public void playAll(CommandSender p, String link, int fade) {
        BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().forEach(((uuid, user) -> {
            user.playAudio(link, fade);
        }));
    }

    @CommandPermission("boostedaudio.audio")
    @Subcommand("playradius")
    public void playradius(Player p, String link, double radius, int fade) {
        playradius(p.getLocation(), radius, link, fade);
    }

    @CommandPermission("boostedaudio.audio")
    @Subcommand("playradius")
    public void playradius(CommandSender p, String link, double radius, int fade) {
        if (!(p instanceof BlockCommandSender block)) return;
        playradius(block.getBlock().getLocation(), radius, link, fade);
    }

    @CommandPermission("boostedaudio.audio")
    @Subcommand("playradius")
    public void playradius(CommandSender p, World world, double x, double y, double z, String link, double radius, int fade) {
        playradius(new Location(world, x, y, z), radius, link, fade);
    }

    private void playradius(Location loc, double radius, String link, int fade) {
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

    @CommandPermission("boostedaudio.audio")
    @Subcommand("stop")
    public void stop(CommandSender p, String link, Player listener) {
        BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(listener.getUniqueId()).stopAudio(link);
    }

    @CommandPermission("boostedaudio.audio")
    @Subcommand("stop")
    public void stop(CommandSender p, String link) {
        BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().forEach(((uuid, user) -> {
            user.stopAudio(link);
        }));
    }

    @CommandPermission("boostedaudio.audio")
    @Subcommand("stopradius")
    public void stopradius(Player p, String link, double radius) {
        stopRadius(p.getLocation(), link, radius);
    }

    @CommandPermission("boostedaudio.audio")
    @Subcommand("stopradius")
    public void stopradius(BlockCommandSender p, String link, double radius) {
        stopRadius(p.getBlock().getLocation(), link, radius);
    }

    @CommandPermission("boostedaudio.audio")
    @Subcommand("stopradius")
    public void stopradius(CommandSender p, World world, double x, double y, double z, String link, double radius) {
        stopRadius(new Location(world, x, y, z), link, radius);
    }

    private void stopRadius(Location loc, String link, double radius) {
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

    @Subcommand("userlist")
    @CommandPermission("boostedaudio.userlist")
    public void userlist(CommandSender sender) {
        sender.sendMessage(Lang.get("users_on_server"));
        StringJoiner joiner = new StringJoiner("§8, ");
        BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().values().forEach(u -> {
            joiner.add((u.isMuted() ? "§c§l" : "§f§l") + Bukkit.getPlayer(u.getPlayerId()).getName());
        });
        sender.sendMessage(joiner.toString());
    }

    @CommandPermission("boostedaudio.mute")
    @Subcommand("mute")
    public void mute(CommandSender sender, Player toMute, float durationMinutes) {
        try {
            User user = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(toMute.getUniqueId());
            if (user.isMuted()) {
                sender.sendMessage(Lang.get("player_already_muted"));
                return;
            }
            long endTime = (long) (System.currentTimeMillis() + 1000 * 60 * durationMinutes);
            user.setMuted(true, endTime);
            sender.sendMessage(Lang.get("player_now_muted", toMute.getName()));
        } catch (Exception ignored) {
        }
    }

    @CommandPermission("unmute")
    @Subcommand("unmute")
    public void unmute(CommandSender sender, Player toUnmute) {
        User user = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(toUnmute.getUniqueId());
        if (!user.isMuted()) {
            sender.sendMessage(Lang.get("player_already_unmuted"));
            return;
        }
        user.setMuted(false, 0);
        sender.sendMessage(Lang.get("player_now_unmuted", toUnmute.getName()));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(new String[]{
                "§8§l[§9§lBoostedAudio§8§l] §7v§f" + BoostedAudioSpigot.getInstance().getPluginVersion() + " " + (Limiter.isPremium() ? "§6§lPremium" : "§aFree"),
                "",
                "§7/boostedaudio help §8- " + Lang.get("command_help"),
                //"§7/boostedaudio reload §8- §7Reload the plugin (can make the server freeze temporarily, and kick all players)",
                "§7/boostedaudio edit §8-" + Lang.get("command_open_edition"),
                "§7/boostedaudio showall §8-" + Lang.get("command_showall"),
                "",
                "§7/boostedaudio userlist §8- " + Lang.get("command_userlist"),
                "",
                "§7/boostedaudio mute " + Lang.get("command_mute"),
                "§7/boostedaudio unmute " + Lang.get("command_unmute"),
                "",
                "§7/boostedaudio download " + Lang.get("command_download"),
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

}