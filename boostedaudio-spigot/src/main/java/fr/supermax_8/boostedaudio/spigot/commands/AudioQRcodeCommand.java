package fr.supermax_8.boostedaudio.spigot.commands;

import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;

import java.util.HashSet;
import java.util.UUID;

public class AudioQRcodeCommand {

    public static HashSet<UUID> requestingQRcode = new HashSet<>();

    @Command({"audioqrcode", "musicqrcode"})
    public boolean onCommand(Player p) {
        requestingQRcode.add(p.getUniqueId());
        AudioCommandSpigot.sendConnectMessage(p);
        return false;
    }


}