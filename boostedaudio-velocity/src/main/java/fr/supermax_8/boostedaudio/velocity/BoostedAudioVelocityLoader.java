package fr.supermax_8.boostedaudio.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.supermax_8.boostedaudio.core.BoostedAudioLoader;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "boostedaudio",
        name = "BoostedAudioVelocity",
        version = "2.7.0",
        description = "Velocity implementation of BoostedAudio, proximitychat and music plugin",
        authors = {"SuperMax_8"}
)
public class BoostedAudioVelocityLoader {

    private Logger logger;
    private final ProxyServer server;
    private final Path dataDirectory;

    @Inject
    public BoostedAudioVelocityLoader(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        long elapsedTime = BoostedAudioLoader.loadLibs(dataDirectory.toFile());
        System.out.println("Libs loaded in " + elapsedTime + " ms");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        BoostedAudioVelocity plugin = new BoostedAudioVelocity(server, logger, dataDirectory);
        server.getEventManager().register(this, plugin);
        plugin.load();
    }


}