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
import fr.supermax_8.boostedaudio.core.utils.UpdateChecker;
import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfiguration;
import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfigurationSection;
import fr.supermax_8.boostedaudio.core.utils.configuration.LazyConfigUpdater;
import fr.supermax_8.boostedaudio.core.websocket.Audio;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;
import fr.supermax_8.boostedaudio.core.websocket.packets.ServerChangePacket;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiConsumer;

public final class BoostedAudioBungee extends Plugin implements Listener {

    private static BoostedAudioBungee instance;

    private BoostedAudioHost host;
    private BoostedAudioConfiguration configuration;
    private VoiceChatManager voiceChatManager;

    private final HashMap<String, PluginMessageResponse> pluginMessageListeners = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
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

        getProxy().getPluginManager().registerCommand(this, new AudioCommandBungee("audio"));

        BoostedAudioAPIImpl.internalAPI = new InternalAPI() {
            @Override
            public String getUsername(UUID uuid) {
                return ProxyServer.getInstance().getPlayer(uuid).getName();
            }
        };

        host = new BoostedAudioHost(configuration);
        voiceChatManager = new VoiceChatManager();

        registerPluginMessage("fromproxy");
        registerPluginMessage("tick", (message, sender, receiver) -> {
            VoiceChatResult voiceChatResult = BoostedAudioAPI.api.getGson().fromJson(message, VoiceChatResult.class);
            voiceChatManager.processResult(voiceChatResult);
        });
        registerPluginMessage("audiotoken", (message, sender, receiver) -> {
            UUID playerId = UUID.fromString(message);
            String audioToken = host.getWebSocketServer().manager.generateConnectionToken(playerId);
            if (audioToken == null) return;
            sendPluginMessage(getServerOfSender(sender), "audiotoken",
                    playerId + ";" + audioToken
            );
            BoostedAudioAPI.getAPI().debug("Audio token sent to " + playerId);
        });
        registerPluginMessage("usersfromuuids", (message, sender, receiver) -> {
            HashSet<String> uuids = new HashSet<>();
            Collections.addAll(uuids, message.split(";"));
            List<HostUser> requestedUsers = host.getWebSocketServer().manager.getUsers().entrySet().stream()
                    .filter(en -> uuids.contains(en.getKey().toString()))
                    .map(en -> (HostUser) en.getValue())
                    .toList();

            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            sendPluginMessage(getServerOfSender(sender), "usersfromuuids", gson.toJson(new UsersFromUuids(requestedUsers)));
        });
        registerPluginMessage("senduserpacket", (message, sender, receiver) -> {
            String[] split = message.split(";", 2);
            UUID uuid = UUID.fromString(split[0]);
            String packet = split[1];
            host.getWebSocketServer().manager.getUsers().get(uuid).sendPacket(packet);
        });
        registerPluginMessage("closeuser", (message, sender, receiver) -> {
            UUID uuid = UUID.fromString(message);
            host.getWebSocketServer().manager.getUsers().get(uuid).close();
        });

        ProxyServer.getInstance().getPluginManager().registerListener(this, this);
        checkForUpdates();
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

    private void checkForUpdates() {
        try {
            if (!configuration.isNotification()) return;
            BoostedAudioAPI.api.info("Checking for updates...");
            new UpdateChecker(112747).getVersion(v -> {
                if (v.equals(getPluginVersion())) return;
                BoostedAudioAPI.api.info("§aNew version available : §6" + v + " §ayou are on §7" + getPluginVersion());
            });

        } catch (Exception ignored) {
        }
    }

    private String getPluginVersion() {
        return getDescription().getVersion();
    }

    public void registerPluginMessage(String channel) {
        registerPluginMessage(channel, null);
    }

    public void registerPluginMessage(String channel, PluginMessageResponse onReceive) {
        String tag = "boostedaudio:" + channel;
        if (onReceive != null) pluginMessageListeners.put(tag, onReceive);
        ProxyServer.getInstance().registerChannel(tag);
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent e) {
        User user = host.getWebSocketServer().manager.getUsers().get(e.getPlayer().getUniqueId());
        if (user != null) user.close();
    }

    @EventHandler
    public void onSwitchServ(ServerConnectedEvent e) {
        //System.out.println("SWITCH SERVER");
        HostUser user = (HostUser) host.getWebSocketServer().manager.getUsers().get(e.getPlayer().getUniqueId());
        if (user == null) {
            if (configuration.isSendOnConnect())
                AudioCommandBungee.sendConnectMessage(e.getPlayer(), e.getServer().getInfo().getName());
            return;
        }
        user.sendPacket(new ServerChangePacket(e.getServer().getInfo().getName()));
        user.waitUntil(500);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        try {
            if (!e.getTag().startsWith("boostedaudio:")) return;
            String message = new String(e.getData());
            PluginMessageResponse response = pluginMessageListeners.get(e.getTag());
            if (response != null) response.onResponse(message, e.getSender(), e.getReceiver());
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static void sendPluginMessage(String server, String channel, String message) {
        ProxyServer.getInstance().getServersCopy().get(server).sendData("boostedaudio:" + channel, message.getBytes());
    }

    private String getServerOfSender(Connection sender) {
        SocketAddress address = sender.getSocketAddress();
        for (Map.Entry<String, ServerInfo> entry : ProxyServer.getInstance().getServersCopy().entrySet()) {
/*            for (ProxiedPlayer player : entry.getValue().getPlayers()) {
                if (player.getSocketAddress().equals(address)) {
                    System.out.println("PLAYERRRRRR");
                    return entry.getKey();
                }
            }*/
            if (entry.getValue().getSocketAddress().equals(address)) {
                // System.out.println("SERVERRRRRR");
                return entry.getKey();
            }
        }
        System.out.println("Problem with sender : " + sender);
        return null;
    }

    public interface PluginMessageResponse {

        void onResponse(String message, Connection sender, Connection receiver);

    }

    public static BoostedAudioBungee getInstance() {
        return instance;
    }

    public BoostedAudioHost getHost() {
        return host;
    }

    public BoostedAudioConfiguration getConfiguration() {
        return configuration;
    }

}