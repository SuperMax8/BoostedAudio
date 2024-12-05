package fr.supermax_8.boostedaudio.spigot.commands;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.core.utils.Lang;
import fr.supermax_8.boostedaudio.core.websocket.packets.ClientMutePacket;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;

public class MuteCommand {

    @Command({"mute", "mutetoggle", "mt"})
    public boolean mute(Player p) {
        User user = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(p.getUniqueId());
        if (user == null) {
            p.performCommand("audio");
            return false;
        }
        user.sendPacket(new ClientMutePacket());

        BoostedAudioSpigot.getInstance().getScheduler().runLaterAsync(() -> {
            User usr = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(p.getUniqueId());
            if (usr == null) return;
            if (usr.isClientMuted()) p.sendMessage(Lang.get("youreNowMuted"));
            else p.sendMessage(Lang.get("youreNowUnMuted"));
        }, 3);
        return false;
    }


}