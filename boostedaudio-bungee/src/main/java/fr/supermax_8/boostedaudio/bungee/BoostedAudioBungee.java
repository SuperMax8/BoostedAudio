package fr.supermax_8.boostedaudio.bungee;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.HostProvider;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.bungee.utils.BungeeCrossConfiguration;
import fr.supermax_8.boostedaudio.bungee.utils.BungeeCrossConfigurationSection;
import fr.supermax_8.boostedaudio.core.*;
import fr.supermax_8.boostedaudio.core.pluginmessage.UsersFromUuids;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceChatManager;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceChatResult;
import fr.supermax_8.boostedaudio.core.utils.ResourceUtils;
import fr.supermax_8.boostedaudio.core.utils.configuration.ConfigUpdater;
import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfiguration;
import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfigurationSection;
import fr.supermax_8.boostedaudio.core.utils.configuration.LazyConfigUpdater;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class BoostedAudioBungee extends Plugin implements Listener {

    private BoostedAudioHost host;
    private BoostedAudioConfiguration configuration;
    private VoiceChatManager voiceChatManager;

    @Override
    public void onEnable() {
        try {
            BoostedAudioLoader.loadExternalLibs(getDataFolder());
        } catch (Exception e) {
            e.printStackTrace();
        }
        CrossConfiguration.instancer = BungeeCrossConfiguration::new;
        CrossConfigurationSection.converter = o -> new BungeeCrossConfigurationSection((Configuration) o);

        loadConf();
        BoostedAudioAPIImpl.configuration = configuration;
        BoostedAudioAPIImpl.hostProvider = new HostProvider() {
            @Override
            public Map<UUID, User> getUsersOnServer() {
                return host.getWebSocketServer().manager.getUsers();
            }

            @Override
            public void waitUntilPluginSetup() {

            }
        };

        BoostedAudioAPIImpl.internalAPI = new InternalAPI() {
            @Override
            public String getUsername(UUID uuid) {
                return ProxyServer.getInstance().getPlayer(uuid).getName();
            }
        };

        host = new BoostedAudioHost(configuration);
        voiceChatManager = new VoiceChatManager();

        ProxyServer.getInstance().registerChannel("boostedaudio:fromproxy");
        ProxyServer.getInstance().registerChannel("boostedaudio:tick");
        ProxyServer.getInstance().registerChannel("boostedaudio:audiotoken");
        ProxyServer.getInstance().registerChannel("boostedaudio:usersfromuuids");

/*        ProxyServer.getInstance().getScheduler().schedule(this, () -> {
            System.out.println("Broadcasting message to all servers");
            ProxyServer.getInstance().getServersCopy().forEach((s, info) -> {
                info.sendData("boostedaudio:fromproxy", "From bungee messaaaaaage !!!!!!!".getBytes());
            });
        }, 0, 5, TimeUnit.SECONDS);*/
        ProxyServer.getInstance().getPluginManager().registerListener(this, this);
    }

    private void loadConf() {
        getDataFolder().mkdirs();
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) try (InputStream in = getResourceAsStream("config.yml")) {
            Files.copy(in, configFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            //ConfigUpdater.update(this::getResourceAsStream, "config.yml", new File(getDataFolder(), "config.yml"));
            LazyConfigUpdater.update(CrossConfiguration.newConfig().load(configFile), ResourceUtils.getResourceAsStream("config.yml"), configFile);
        } catch (Exception ignored) {
        }
        configuration = new BoostedAudioConfiguration(configFile);
    }


    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        System.out.println("RECEIVED MESSAGE FROM " + e.getSender() + " : " + e.getTag() + " : " + new String(e.getData()));
        if (!e.getTag().startsWith("boostedaudio:")) return;
        String message = new String(e.getData());
        switch (e.getTag()) {
            case "boostedaudio:tick":
                System.out.println("Tick");
                VoiceChatResult voiceChatResult = BoostedAudioAPI.api.getGson().fromJson(message, VoiceChatResult.class);
                if (configuration.isDebugMode()) {
                    System.out.println("PRINT");
                    System.out.println(message);
                }
                voiceChatManager.processResult(voiceChatResult);
                break;
            case "boostedaudio:audiotoken":
                UUID playerId = UUID.fromString(message);
                String audioToken = host.getWebSocketServer().manager.generateConnectionToken(playerId);
                sendPluginMessage(getServerOfSender(e.getSender()), "audiotoken",
                        playerId + ";" + audioToken
                );
                break;
            case "boostedaudio:usersfromuuids":
                HashSet<String> uuids = new HashSet<>();
                Collections.addAll(uuids, message.split(";"));
                List<HostUser> requestedUsers = host.getWebSocketServer().manager.getUsers().entrySet().stream()
                        .filter(en -> uuids.contains(en.getKey().toString()))
                        .map(en -> (HostUser) en.getValue())
                        .toList();

                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                sendPluginMessage(getServerOfSender(e.getSender()), "usersfromuuids", gson.toJson(new UsersFromUuids(requestedUsers)));
                break;
        }
    }

    private void sendPluginMessage(String server, String channel, String message) {
        ProxyServer.getInstance().getServersCopy().get(server).sendData("boostedaudio:" + channel, message.getBytes());
    }

    private String getServerOfSender(Connection sender) {
        SocketAddress address = sender.getSocketAddress();
        for (Map.Entry<String, ServerInfo> entry : ProxyServer.getInstance().getServersCopy().entrySet()) {
            for (ProxiedPlayer player : entry.getValue().getPlayers()) {
                if (player.getSocketAddress().equals(address)) {
                    System.out.println("PLAYERRRRRR");
                    return entry.getKey();
                }
            }
            if (entry.getValue().getSocketAddress().equals(address)) {
                System.out.println("SERVERRRRRR");
                return entry.getKey();
            }
        }
        System.out.println("Problem with sender : " + sender);
        return null;
    }

}