package fr.supermax_8.boostedaudio.bungee;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.HostProvider;
import fr.supermax_8.boostedaudio.api.user.User;
import fr.supermax_8.boostedaudio.bungee.utils.BungeeCrossConfiguration;
import fr.supermax_8.boostedaudio.bungee.utils.BungeeCrossConfigurationSection;
import fr.supermax_8.boostedaudio.core.*;
import fr.supermax_8.boostedaudio.core.serverpacket.ServerPacketListener;
import fr.supermax_8.boostedaudio.core.serverpacket.ServerUser;
import fr.supermax_8.boostedaudio.core.serverpacket.UsersFromUuids;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceChatManager;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceChatResult;
import fr.supermax_8.boostedaudio.core.utils.ResourceUtils;
import fr.supermax_8.boostedaudio.core.utils.UpdateChecker;
import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfiguration;
import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfigurationSection;
import fr.supermax_8.boostedaudio.core.utils.configuration.LazyConfigUpdater;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.ConnectionManager;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;
import fr.supermax_8.boostedaudio.core.websocket.packets.ServerChangePacket;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.*;

public final class BoostedAudioBungee extends Plugin implements Listener {

    private static BoostedAudioBungee instance;

    private BoostedAudioHost host;
    private BoostedAudioConfiguration configuration;
    private VoiceChatManager voiceChatManager;

    private final HashMap<String, ServerPacketListener> pluginMessageListeners = new HashMap<>();

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

        ProxyServer.getInstance().registerChannel("boostedaudio:servername");

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
        AudioWebSocketServer.serverProxyCheck = (s, webSocket) -> {
            System.out.println("serverProxyCheck Tick");
            try {
                String[] split = s.split(";", 2);
                String token = split[0];
                String serverName = split[1];
                if (serverName.equals("?")) {
                    sendServerNames();
                    System.out.println("Server names sent");
                    return true;
                }

                for (String secret : configuration.getBungeeSecrets()) {
                    if (secret.equals(token)) {
                        UUID uuid = UUID.randomUUID();
                        ConnectionManager manager = AudioWebSocketServer.getInstance().manager;
                        ServerUser serverUser = new ServerUser(secret, serverName, uuid, webSocket);
                        manager.getServers().put(uuid, serverUser);
                        manager.getSessionUsers().put(webSocket, Optional.of(serverUser));
                        System.out.println("Server connected : " + serverName);
                        return true;
                    }
                }
            } catch (Exception e) {
            }
            return false;
        };

        AudioWebSocketServer.proxyConsumer = (message, serverUser) -> {
            String[] split = message.split(";", 2);
            ServerPacketListener listener = pluginMessageListeners.get(split[0]);
            if (listener == null) return;
            listener.onReceive(split[1], serverUser.getServerId());
            /*System.out.println("Received message from server " + serverUser.getServerId() + " : " + message);*/
        };

        voiceChatManager = new VoiceChatManager();

        registerServerListener("tick", (message, serverid) -> {
            VoiceChatResult voiceChatResult = BoostedAudioAPI.api.getGson().fromJson(message, VoiceChatResult.class);
            voiceChatManager.processResult(voiceChatResult);
        });
        registerServerListener("audiotoken", (message, serverId) -> {
            UUID playerId = UUID.fromString(message);
            String audioToken = host.getWebSocketServer().manager.generateConnectionToken(playerId);
            if (audioToken == null) return;
            sendServerPacket(serverId, "audiotoken",
                    playerId + ";" + audioToken
            );
            BoostedAudioAPI.getAPI().debug("Audio token sent to " + playerId);
        });
        registerServerListener("usersfromuuids", (message, serverId) -> {
            HashSet<String> uuids = new HashSet<>();
            Collections.addAll(uuids, message.split(";"));
            List<HostUser> requestedUsers = host.getWebSocketServer().manager.getUsers().entrySet().stream()
                    .filter(en -> uuids.contains(en.getKey().toString()))
                    .map(en -> (HostUser) en.getValue())
                    .toList();

            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            sendServerPacket(serverId, "usersfromuuids", gson.toJson(new UsersFromUuids(requestedUsers)));
        });
        registerServerListener("senduserpacket", (message, serverId) -> {
            String[] split = message.split(";", 2);
            UUID uuid = UUID.fromString(split[0]);
            String packet = split[1];
            host.getWebSocketServer().manager.getUsers().get(uuid).sendPacket(packet);
        });
        registerServerListener("closeuser", (message, serverId) -> {
            UUID uuid = UUID.fromString(message);
            host.getWebSocketServer().manager.getUsers().get(uuid).close();
        });

        ProxyServer.getInstance().getPluginManager().registerListener(this, this);
        checkForUpdates();
    }

    private void sendServerNames() {
        for (ServerInfo info : ProxyServer.getInstance().getServersCopy().values()) {
            info.sendData("boostedaudio:servername", info.getName().getBytes());
        }
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

    public void registerServerListener(String channel) {
        registerServerListener(channel, null);
    }

    public void registerServerListener(String channel, ServerPacketListener onReceive) {
        if (onReceive != null) pluginMessageListeners.put(channel, onReceive);
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

    public static void sendServerPacket(UUID server, String channel, String message) {
        AudioWebSocketServer.getInstance().manager.getServers().get(server).getWebSocket().send(channel + ";" + message);
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