package fr.supermax_8.boostedaudio.velocity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.HostProvider;
import fr.supermax_8.boostedaudio.api.user.Audio;
import fr.supermax_8.boostedaudio.api.user.User;
import fr.supermax_8.boostedaudio.core.*;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceChatManager;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceChatResult;
import fr.supermax_8.boostedaudio.core.serverpacket.ServerPacketListener;
import fr.supermax_8.boostedaudio.core.serverpacket.ServerUser;
import fr.supermax_8.boostedaudio.core.serverpacket.UsersFromUuids;
import fr.supermax_8.boostedaudio.core.utils.UpdateChecker;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.ConnectionManager;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;
import fr.supermax_8.boostedaudio.core.websocket.packets.ServerChangePacket;
import lombok.Getter;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BoostedAudioVelocity {

    private static String fqsfdsqfdsq = "%%__USER__%% %%__RESOURCE__%% %%__NONCE__%% %%__USER__%% %%__RESOURCE__%% %%__NONCE__%%";
    private static final String version = "2.6.3";
    private Logger logger;
    private final ProxyServer server;
    private final Path dataDirectory;

    @Getter
    private static BoostedAudioVelocity instance;

    @Getter
    private BoostedAudioHost host;
    @Getter
    private BoostedAudioConfiguration configuration;
    private VoiceChatManager voiceChatManager;

    private final HashMap<String, ServerPacketListener> pluginMessageListeners = new HashMap<>();

    private final MinecraftChannelIdentifier serverNameChannel = MinecraftChannelIdentifier.from("boostedaudio:servername");

    public BoostedAudioVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    public void load() {
        BoostedAudioAPIImpl.sendMessage = s -> logger.info(s);
        logger.info("Initializing...");
        instance = this;
/*        BoostedAudioAPIImpl.sendMessage = s -> logger.info(s);
        try {
            dataDirectory.toFile().mkdirs();
            BoostedAudioLoader.loadExternalLibs(dataDirectory.toFile());
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        server.getChannelRegistrar().register(serverNameChannel);

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

        CommandManager commandManager = server.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("audio")
                .plugin(this)
                .build();
        commandManager.register(commandMeta, new AudioCommandVelocity());

        BoostedAudioAPIImpl.internalAPI = new InternalAPI() {
            @Override
            public String getUsername(UUID uuid) {
                return server.getPlayer(uuid).get().getUsername();
            }
        };

        host = new BoostedAudioHost(configuration);
        AudioWebSocketServer.serverProxyCheck = (s, webSocket) -> {
            BoostedAudioAPI.getAPI().debug("Try to connect " + webSocket.getRemoteSocketAddress() + " " + s);
            try {
                String[] split = s.split(";", 2);
                String token = split[0];
                String serverName = split[1];
                if (serverName.equals("?")) {
                    sendServerNames();
                    return true;
                }

                for (String secret : configuration.getBungeeSecrets()) {
                    if (secret.equals(token)) {
                        UUID uuid = UUID.randomUUID();
                        ConnectionManager manager = AudioWebSocketServer.getInstance().manager;
                        ServerUser serverUser = new ServerUser(secret, serverName, uuid, webSocket);
                        manager.getServers().put(uuid, serverUser);
                        manager.getSessionUsers().put(webSocket, Optional.of(serverUser));
                        BoostedAudioAPI.getAPI().info("Server connected : " + serverName);
                        return true;
                    }
                }
                BoostedAudioAPI.getAPI().info("WRONG MULTISERV SECRET : " + serverName + " " + webSocket.getRemoteSocketAddress().toString());
            } catch (Exception e) {
            }
            return false;
        };

        AudioWebSocketServer.proxyConsumer = (message, serverUser) -> {
            String[] split = message.split(";", 2);
            ServerPacketListener listener = pluginMessageListeners.get(split[0]);
            if (listener == null) return;
            listener.onReceive(split[1], serverUser.getServerId());
            /*BoostedAudioAPI.getAPI().info("Received message from server " + serverUser.getServerId() + " : " + message);*/
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
        registerServerListener("setmuted", (message, serverId) -> {
            String[] split = message.split(";", 3);

            UUID uuid = UUID.fromString(split[0]);
            host.getWebSocketServer().manager.getUsers().get(uuid).setMuted(Boolean.parseBoolean(split[1]), Long.parseLong(split[2]));
        });

        registerServerListener("playaudio", (message, serverId) -> {
            String[] split = message.split(";", 2);

            UUID uuid = UUID.fromString(split[0]);
            Audio audio = BoostedAudioAPI.getAPI().getGson().fromJson(split[1], Audio.class);
            host.getWebSocketServer().manager.getUsers().get(uuid).playAudio(audio);
        });
        registerServerListener("removeaudio", (message, serverId) -> {
            String[] split = message.split(";", 2);

            UUID uuid = UUID.fromString(split[0]);
            Audio audio = BoostedAudioAPI.getAPI().getGson().fromJson(split[1], Audio.class);
            host.getWebSocketServer().manager.getUsers().get(uuid).stopAudio(audio);
        });
        registerServerListener("pauseaudio", (message, serverId) -> {
            String[] split = message.split(";", 2);

            UUID uuid = UUID.fromString(split[0]);
            Audio audio = BoostedAudioAPI.getAPI().getGson().fromJson(split[1], Audio.class);
            host.getWebSocketServer().manager.getUsers().get(uuid).pauseAudio(audio);
        });

        checkForUpdates();
    }

    private void sendServerNames() {
        BoostedAudioAPI.getAPI().debug("Sending server names");
        for (RegisteredServer server : server.getAllServers()) {
            server.sendPluginMessage(serverNameChannel, server.getServerInfo().getName().getBytes());
        }
    }

    private void loadConf() {
        File configFile = new File(dataDirectory.toFile(), "config.yml");
/*        if (!configFile.exists()) try (InputStream in = getResourceAsStream("config.yml")) {
            Files.copy(in, configFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            //ConfigUpdater.update(this::getResourceAsStream, "config.yml", new File(getDataFolder(), "config.yml"));
            LazyConfigUpdater.update(CrossConfiguration.newConfig().load(configFile), getResourceAsStream("config.yml"), configFile);
        } catch (Exception ignored) {
        }*/
        configuration = new BoostedAudioConfiguration(configFile);
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
        return version;
    }

    public void registerServerListener(String channel) {
        registerServerListener(channel, null);
    }

    public void registerServerListener(String channel, ServerPacketListener onReceive) {
        if (onReceive != null) pluginMessageListeners.put(channel, onReceive);
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent e) {
        CompletableFuture.runAsync(() -> {
            User user = host.getWebSocketServer().manager.getUsers().get(e.getPlayer().getUniqueId());
            if (user != null) user.close();
        });
    }

    @Subscribe
    public void onSwitchServ(ServerConnectedEvent e) {
        CompletableFuture.runAsync(() -> {
            HostUser user = (HostUser) host.getWebSocketServer().manager.getUsers().get(e.getPlayer().getUniqueId());
            if (user == null) {
                if (configuration.isSendOnConnect())
                    AudioCommandVelocity.sendConnectMessage(e.getPlayer(), e.getServer().getServerInfo().getName());
                return;
            }
            user.sendPacket(new ServerChangePacket(e.getServer().getServerInfo().getName()));
            user.waitUntil(500);
        });
    }

    public static void sendServerPacket(UUID server, String channel, String message) {
        AudioWebSocketServer.getInstance().manager.getServers().get(server).getWebSocket().send(channel + ";" + message);
    }


}