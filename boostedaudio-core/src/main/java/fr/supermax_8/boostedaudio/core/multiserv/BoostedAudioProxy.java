package fr.supermax_8.boostedaudio.core.multiserv;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.HostProvider;
import fr.supermax_8.boostedaudio.api.audio.Audio;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.core.BoostedAudioAPIImpl;
import fr.supermax_8.boostedaudio.core.BoostedAudioConfiguration;
import fr.supermax_8.boostedaudio.core.BoostedAudioHost;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceChatManager;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceChatResult;
import fr.supermax_8.boostedaudio.core.utils.MediaDownloader;
import fr.supermax_8.boostedaudio.core.utils.UpdateChecker;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.ConnectionManager;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;
import fr.supermax_8.boostedaudio.core.websocket.packets.ServerChangePacket;
import lombok.Getter;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Getter
public class BoostedAudioProxy {

    private BoostedAudioHost host;
    private BoostedAudioConfiguration configuration;
    private VoiceChatManager voiceChatManager;
    private DiffuserWebSocketClient diffuserWebSocketClient;
    private final HashMap<String, ServerPacketListener> pluginMessageListeners = new HashMap<>();
    private final File dataFolder;
    private final String pluginVersion;

    public BoostedAudioProxy(File dataFolder, String pluginVersion) {
        this.dataFolder = dataFolder;
        this.pluginVersion = pluginVersion;
        loadConf();
        BoostedAudioAPIImpl.configuration = this.configuration;
        checkForUpdates();
        if (configuration.isDiffuser()) setupDiffuser();
        else setupHost();
    }

    private void setupHost() {
        BoostedAudioAPI.getAPI().debug("Setup Host");
        BoostedAudioAPIImpl.hostProvider = new HostProvider() {
            @Override
            public Map<UUID, User> getUsersOnServer() {
                return host.getWebSocketServer().manager.getUsers();
            }

            @Override
            public void waitUntilPluginSetup() {

            }
        };
        host = new BoostedAudioHost(configuration);
        AudioWebSocketServer.serverProxyCheck = (s, webSocket) -> {
            BoostedAudioAPI.getAPI().debug("Try to connect " + webSocket.getRemoteSocketAddress() + " " + s);
            try {
                String[] split = s.split(";", 2);
                String token = split[0];
                String serverName = split[1];

                for (String secret : configuration.getSecrets()) {
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
                BoostedAudioAPI.getAPI().info("WRONG BUNGEE SECRET : " + serverName + " " + webSocket.getRemoteSocketAddress().toString());
            } catch (Exception ignored) {
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
            String link = configuration.getClientLink()
                    + "?t="
                    + audioToken;
            sendServerPacket(serverId, "audiotoken",
                    playerId + ";" + link
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


        registerServerListener("switchserver", (message, serverId) -> {
            String[] split = message.split(";", 2);

            UUID uuid = UUID.fromString(split[0]);
            String serverName = split[1];

            onSwitchServer(uuid, serverName);
        });
        registerServerListener("disconnect", (message, serverId) -> {
            UUID uuid = UUID.fromString(message);
            onDisconnect(uuid);
        });

        registerServerListener("downloadaudio", (message, serverId) -> {
            CompletableFuture.runAsync(() -> {
                File dir = new File(configuration.getDataFolder(), "webhost" + File.separator + "audio" + File.separator + "downloaded");
                String fileName = MediaDownloader.download(message, BoostedAudioAPI.getAPI().getConfiguration().getAudioDownloaderFormat(), dir);
                System.out.println("DL ended");
                sendServerPacket(serverId, "downloadaudio", "audio/downloaded/" + fileName);
            });
        });
    }

    private void setupDiffuser() {
        BoostedAudioAPI.getAPI().debug("Setup Diffuser");
        try {
            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
                if (diffuserWebSocketClient != null && diffuserWebSocketClient.isConnected()) return;
                CompletableFuture.runAsync(() -> {
                    try {
                        if (diffuserWebSocketClient != null) diffuserWebSocketClient.close();
                        BoostedAudioAPI.getAPI().debug("Diffuser try to connect...");
                        diffuserWebSocketClient = new DiffuserWebSocketClient(new URI(configuration.getMainProxyWebsocketLink()));
                        diffuserWebSocketClient.connect();
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
            }, 0, 2, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerServerListener(String channel, ServerPacketListener onReceive) {
        if (onReceive != null) pluginMessageListeners.put(channel, onReceive);
    }

    public void sendServerPacket(UUID server, String channel, String message) {
        AudioWebSocketServer.getInstance().manager.getServers().get(server).getWebSocket().send(channel + ";" + message);
    }

    private void loadConf() {
        dataFolder.mkdirs();
        File configFile = new File(dataFolder, "config.yml");
        configuration = new BoostedAudioConfiguration(configFile);
    }

    private void checkForUpdates() {
        try {
            if (!configuration.isNotification()) return;
            BoostedAudioAPI.api.info("Checking for updates...");
            new UpdateChecker(112747).getVersion(v -> {
                if (v.equals(pluginVersion) || pluginVersion.contains("Stand")) return;
                BoostedAudioAPI.api.info("§aNew version available : §6" + v + " §ayou are on §7" + pluginVersion);
            });

        } catch (Exception ignored) {
        }
    }

    public void onSwitchServer(UUID playerId, String newServerName) {
        CompletableFuture.runAsync(() -> {
            if (configuration.isDiffuser()) {
                diffuserWebSocketClient.send("switchserver;" + playerId + ";" + newServerName);
                return;
            }
            HostUser user = (HostUser) host.getWebSocketServer().manager.getUsers().get(playerId);
            if (user == null) return;

            user.sendPacket(new ServerChangePacket(newServerName));
            user.waitUntil(500);
        });
    }

    public void onDisconnect(UUID playerId) {
        CompletableFuture.runAsync(() -> {
            if (configuration.isDiffuser()) {
                diffuserWebSocketClient.send("disconnect;" + playerId);
                return;
            }
            User user = host.getWebSocketServer().manager.getUsers().get(playerId);
            if (user != null) user.close();
        });
    }

}