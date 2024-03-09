package fr.supermax_8.boostedaudio.spigot.manager;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.core.BoostedAudioConfiguration;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceHoldersManager extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "boostedaudio";
    }

    @Override
    public String getAuthor() {
        return "SuperMax_8";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (!player.isOnline()) return null;
        switch (params) {
            case "on_audiopanel" -> {
                User user = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(player.getUniqueId());
                return user != null ? "true" : "false";
            }
            case "panel_state" -> {
                BoostedAudioAPI api = BoostedAudioAPI.getAPI();
                User user = api.getHostProvider().getUsersOnServer().get(player.getUniqueId());
                BoostedAudioConfiguration config = api.getConfiguration();
                if (user == null) return config.getNotconnectedSymbol();
                return user.isMuted() ? config.getMutedSymbol() : config.getConnectedSymbol();
            }
        }
        return null;
    }


}