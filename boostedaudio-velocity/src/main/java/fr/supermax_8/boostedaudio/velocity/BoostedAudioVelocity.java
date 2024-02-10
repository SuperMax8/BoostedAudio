package fr.supermax_8.boostedaudio.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.supermax_8.boostedaudio.core.BoostedAudioAPIImpl;
import fr.supermax_8.boostedaudio.core.multiserv.BoostedAudioProxy;
import fr.supermax_8.boostedaudio.core.InternalAPI;
import lombok.Getter;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.UUID;

public class BoostedAudioVelocity {

    private static String fqsfdsqfdsq = "%%__USER__%% %%__RESOURCE__%% %%__NONCE__%% %%__USER__%% %%__RESOURCE__%% %%__NONCE__%%";
    private static final String version = "2.14.0";
    private Logger logger;
    private final ProxyServer server;
    private final Path dataDirectory;

    @Getter
    private static BoostedAudioVelocity instance;
    private BoostedAudioProxy boostedAudioProxy;

    public BoostedAudioVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    public void load() {
        BoostedAudioAPIImpl.sendMessage = s -> logger.info(s);
        logger.info("Initializing...");
        instance = this;
        BoostedAudioAPIImpl.internalAPI = new InternalAPI() {
            @Override
            public String getUsername(UUID uuid) {
                return server.getPlayer(uuid).get().getUsername();
            }
        };
        boostedAudioProxy = new BoostedAudioProxy(dataDirectory.toFile(), getPluginVersion());
    }

    private String getPluginVersion() {
        return version;
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent e) {
        boostedAudioProxy.onDisconnect(e.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onSwitchServ(ServerConnectedEvent e) {
        boostedAudioProxy.onSwitchServer(e.getPlayer().getUniqueId(), e.getServer().getServerInfo().getName());
    }

}