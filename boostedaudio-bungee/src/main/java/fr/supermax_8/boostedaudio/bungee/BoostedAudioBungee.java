package fr.supermax_8.boostedaudio.bungee;

import fr.supermax_8.boostedaudio.core.BoostedAudioAPIImpl;
import fr.supermax_8.boostedaudio.core.InternalAPI;
import fr.supermax_8.boostedaudio.core.multiserv.BoostedAudioProxy;
import fr.supermax_8.boostedaudio.core.utils.MojangAPI;
import fr.supermax_8.boostedaudio.sharedutils.BoostedAudioLoader;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public final class BoostedAudioBungee extends Plugin implements Listener {

    private static String fqsfdsqfdsq = "%%__USER__%% %%__RESOURCE__%% %%__NONCE__%% %%__USER__%% %%__RESOURCE__%% %%__NONCE__%%";

    @Getter
    private static BoostedAudioBungee instance;

    private BoostedAudioProxy boostedAudioProxy;

    @Override
    public void onEnable() {
        instance = this;
        BoostedAudioAPIImpl.sendMessage = s -> ProxyServer.getInstance().getConsole().sendMessage("§8§l[§9§lBoostedAudio§8§l] §7" + s);
        try {
            BoostedAudioLoader.loadExternalLibs(getDataFolder());
        } catch (Exception e) {
            e.printStackTrace();
        }

        BoostedAudioAPIImpl.internalAPI = new InternalAPI() {
            @Override
            public String getUsername(UUID uuid) {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
                if (player != null) return player.getName();
                try {
                    return MojangAPI.getUsernameFromUUID(uuid.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    return " ";
                }
            }
        };

        boostedAudioProxy = new BoostedAudioProxy(getDataFolder(), getPluginVersion());
        ProxyServer.getInstance().getPluginManager().registerListener(this, this);
    }


    @Override
    public void onDisable() {
    }

    private String getPluginVersion() {
        return getDescription().getVersion();
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent e) {
        boostedAudioProxy.onDisconnect(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onSwitchServ(ServerConnectedEvent e) {
        boostedAudioProxy.onSwitchServer(e.getPlayer().getUniqueId(), e.getServer().getInfo().getName());
    }


}