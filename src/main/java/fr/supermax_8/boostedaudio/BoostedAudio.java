package fr.supermax_8.boostedaudio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.supermax_8.boostedaudio.commands.AudioCommand;
import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.PacketList;
import fr.supermax_8.boostedaudio.web.packets.RTCIcePacket;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class BoostedAudio extends JavaPlugin {

    private static BoostedAudio instance;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(PacketList.class, new PacketList.Adapter())
            .registerTypeAdapter(RTCIcePacket.class, new RTCIcePacket.Adapter())
            .create();

    private AudioWebSocketServer webSocketServer;
    private BoostedAudioConfiguration configuration;

    @Override
    public void onEnable() {
        instance = this;
        configuration = new BoostedAudioConfiguration();
        getCommand("audio").setExecutor(new AudioCommand());

        startWebSocket();
    }

    @Override
    public void onDisable() {
        try {
            webSocketServer.stop();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void info(String message) {
        instance.getLogger().info(message);
    }

    public static void debug(String message) {
        if (instance.configuration.isDebugMode()) info(message);
    }

    public static BoostedAudio getInstance() {
        return instance;
    }

    public static Gson getGson() {
        return gson;
    }

    public AudioWebSocketServer getWebSocketServer() {
        return webSocketServer;
    }

    public BoostedAudioConfiguration getConfiguration() {
        return configuration;
    }

    private void startWebSocket() {
        webSocketServer = new AudioWebSocketServer(new InetSocketAddress(configuration.getHostName(), configuration.getPort()));
        webSocketServer.setReuseAddr(true);
        CompletableFuture.runAsync(() -> webSocketServer.run());
    }


}