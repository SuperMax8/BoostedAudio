package fr.supermax_8.boostedaudio.spigot.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;

public class AudioQRcodeCommand implements CommandExecutor {

    public static HashSet<UUID> requestingQRcode = new HashSet<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player p)) return false;
        requestingQRcode.add(p.getUniqueId());
        AudioCommandSpigot.sendConnectMessage(p);
        return false;
    }


}