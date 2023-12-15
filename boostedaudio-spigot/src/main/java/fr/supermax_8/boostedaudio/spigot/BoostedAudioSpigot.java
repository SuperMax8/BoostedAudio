package fr.supermax_8.boostedaudio.spigot;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.HostProvider;
import fr.supermax_8.boostedaudio.api.user.User;
import fr.supermax_8.boostedaudio.core.*;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceChatManager;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceChatResult;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceLayer;
import fr.supermax_8.boostedaudio.core.multiserv.ServerPacketListener;
import fr.supermax_8.boostedaudio.core.multiserv.UsersFromUuids;
import fr.supermax_8.boostedaudio.core.utils.DataVisualisationUtils;
import fr.supermax_8.boostedaudio.core.utils.UpdateChecker;
import fr.supermax_8.boostedaudio.spigot.commands.AudioCommandSpigot;
import fr.supermax_8.boostedaudio.spigot.commands.AudioQRcodeCommand;
import fr.supermax_8.boostedaudio.spigot.commands.BoostedAudioCommand;
import fr.supermax_8.boostedaudio.spigot.diffuser.DiffuserUser;
import fr.supermax_8.boostedaudio.core.multiserv.DiffuserWebSocketClient;
import fr.supermax_8.boostedaudio.spigot.manager.AudioManager;
import fr.supermax_8.boostedaudio.spigot.manager.PlaceHoldersManager;
import fr.supermax_8.boostedaudio.spigot.manager.RegionManager;
import fr.supermax_8.boostedaudio.spigot.proximitychat.VoiceChatProcessor;
import fr.supermax_8.boostedaudio.spigot.utils.AroundManager;
import fr.supermax_8.boostedaudio.spigot.utils.FileUtils;
import fr.supermax_8.boostedaudio.spigot.utils.Scheduler;
import fr.supermax_8.boostedaudio.spigot.utils.TemporaryListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.wildfly.common.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class BoostedAudioSpigot extends JavaPlugin {

    private static String a = "%%__RESOURCE__%% %%__USER__%% %%__NONCE__%% %%__USER__%% %%__RESOURCE__%% %%__NONCE__%%";
    @Getter
    private static BoostedAudioSpigot instance;

    @Nullable
    private BoostedAudioHost host;
    @Nullable
    private VoiceChatManager voiceChatManager;

    @Nullable
    private HostRequester hostRequester;
    @Nullable
    private Map<UUID, User> usersOnServer;
    @Nullable
    private DiffuserWebSocketClient diffuserWebSocketClient;

    @Getter
    private VoiceChatProcessor voiceChatProcessor;
    @Getter
    private AudioManager audioManager;
    @Getter
    private AroundManager aroundManager;
    private BoostedAudioConfiguration configuration;
    private String workingMode = "";

    @Override
    public void onEnable() {
        instance = this;
        usersOnServer = new ConcurrentHashMap<>();

        BoostedAudioAPIImpl.sendMessage = s -> Bukkit.getConsoleSender().sendMessage(s);

        configuration = new BoostedAudioConfiguration(new File(getDataFolder(), "config.yml"));
        BoostedAudioAPIImpl.configuration = configuration;

        getCommand("audio").setExecutor(new AudioCommandSpigot());
        getCommand("boostedaudio").setExecutor(new BoostedAudioCommand());
        getCommand("audioqrcode").setExecutor(new AudioQRcodeCommand());

        checkForUpdates();

        try {
            BoostedAudioLoader.loadExternalLibs(getDataFolder());
        } catch (IOException e) {
            BoostedAudioAPI.api.info("Error while loading external libs, the plugin can't started, maybe the server don't have access to internet connection ? Or the plugin don't have permission to write in the plugin folder ?");
            e.printStackTrace();
            return;
        }

        BoostedAudioAPIImpl.internalAPI = new InternalAPI() {
            @Override
            public String getUsername(UUID uuid) {
                return Bukkit.getPlayer(uuid).getName();
            }
        };

        if (configuration.isDiffuser()) setupDiffuser();
        else setupHost();

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        aroundManager = new AroundManager();

        // Placeholders
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceHolderAPI")) {
            new PlaceHoldersManager().register();
            BoostedAudioAPI.getAPI().info("Placeholders loaded successfully");
        } else BoostedAudioAPI.getAPI().info("PlaceholderAPI is not on the server");

        Scheduler.runTaskLater(() -> {
            try {
                audioManager = new AudioManager();
                BoostedAudioAPI.getAPI().debug("Audio manager loaded");
            } catch (Throwable e) {
                e.printStackTrace();
                BoostedAudioAPI.getAPI().info("Audio manager NOT LOADED. You certainly have problems in you data files with old worlds deleted");
            }

            // Region manager
            RegionManager regionManager = audioManager.getRegionManager();
            if (regionManager != null)
                Scheduler.runTaskTimerAsync(() -> {
                    try {
                        regionManager.tick(BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer());
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }, 0, 0);
        }, 0);

        voiceChatProcessor = new VoiceChatProcessor();
        if (configuration.isVoiceChatEnabled())
            voiceChatProcessor.getLayers().put("proximitychat", new VoiceLayer(true, 0, null, "proximitychat"));

        Bukkit.getScheduler().runTaskLater(this, this::initMetrics, 20 * 60);
    }

    @Override
    public void onDisable() {
        if (diffuserWebSocketClient != null) diffuserWebSocketClient.close();
    }

    private void initMetrics() {
        int pluginId = 19857;
        Metrics metrics = new Metrics(BoostedAudioSpigot.getInstance(), pluginId);
        metrics.addCustomChart(new Metrics.SimplePie("sucessful_setup", () -> String.valueOf(host.isSucessfulSetup())));
        metrics.addCustomChart(new Metrics.SimplePie("ffmpeg_setuped", () -> String.valueOf(FileUtils.ffmpeg != null)));
        metrics.addCustomChart(new Metrics.SingleLineChart("players_connected_to_audio_panel", () ->
                host.getWebSocketServer().manager.getUsers().size()));
        metrics.addCustomChart(new Metrics.SimplePie("nbspeakers", () -> DataVisualisationUtils.intMetricToEzReadString(BoostedAudioSpigot.getInstance().getAudioManager().getSpeakerManager().speakers.size())));
        metrics.addCustomChart(new Metrics.SimplePie("workingmode", () -> workingMode));
        metrics.addCustomChart(new Metrics.SimplePie("ispremium", () -> String.valueOf(Limiter.isPremium())));
        metrics.addCustomChart(new Metrics.SimplePie("nbregions", () -> {
            try {
                return DataVisualisationUtils.intMetricToEzReadString(audioManager.getRegionManager().getAudioRegions().size());
            } catch (Exception e) {
                return "No regions";
            }
        }));
    }

    private void setupHost() {
        // Host mode
        workingMode = "Host";

        host = new BoostedAudioHost(configuration);
        BoostedAudioAPIImpl.hostProvider = new HostProvider() {
            @Override
            public Map<UUID, User> getUsersOnServer() {
                waitUntilPluginSetup();
                return host.getWebSocketServer().manager.getUsers();
            }

            @Override
            public void waitUntilPluginSetup() {
                while (!host.isSucessfulSetup()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        voiceChatManager = new VoiceChatManager();
        Scheduler.runTaskTimerAsync(() -> {
            try {
                VoiceChatResult result = voiceChatProcessor.process();
                voiceChatManager.processResult(result);
            } catch (Throwable e) {
                BoostedAudioAPI.getAPI().info("Error while processing voice chat :");
                e.printStackTrace();
            }
        }, 0, 0);
    }

    private void setupDiffuser() {
        // Diffuser mode
        workingMode = "Diffuser";

        // Init
        Scheduler.runTaskAsync(t -> {
            // Init DiffuserWebSocketClient when a player is online to avoid pluginChannel problems
            BoostedAudioAPI.getAPI().debug("Bungee websocket uri: " + configuration.getMainProxyWebsocketLink());

            Scheduler.runTaskTimerAsync(task -> {
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
            }, 0, 40);

            registerServerPacketListener("audiotoken", (message, serverId) -> {
                String[] split = message.split(";", 2);
                OfflinePlayer player1 = Bukkit.getOfflinePlayer(UUID.fromString(split[0]));
                if (player1.isOnline()) AudioCommandSpigot.sendConnectMessage(player1.getPlayer(), split[1]);
            });

            Scheduler.runTaskTimerAsync(() -> {
                try {
                    StringJoiner joiner = new StringJoiner(";");
                    Bukkit.getOnlinePlayers().forEach(player -> joiner.add(player.getUniqueId().toString()));
                    hostRequester.request("usersfromuuids", joiner.toString(), (UsersFromUuids usersFromUuids) -> {
                        Map<UUID, User> map = new ConcurrentHashMap<>();
                        usersFromUuids.getUsers().forEach(user -> map.put(user.getPlayerId(), new DiffuserUser(user)));
                        usersOnServer = map;
                    }, UsersFromUuids.class);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }, 0, 0);
        });


        hostRequester = new HostRequester();
        BoostedAudioAPIImpl.hostProvider = new HostProvider() {
            @Override
            public Map<UUID, User> getUsersOnServer() {
                waitUntilPluginSetup();
                return usersOnServer;
            }

            @Override
            public void waitUntilPluginSetup() {
            }
        };

        Scheduler.runTaskTimerAsync(() -> {
            try {
                VoiceChatResult result = voiceChatProcessor.process();
                if (result == null) return;
                sendServerPacket("tick", BoostedAudioAPI.api.getGson().toJson(result));
            } catch (Throwable e) {
                BoostedAudioAPI.getAPI().info("Error while processing voice chat :");
                e.printStackTrace();
            }
        }, 0, 0);
    }


    private void checkForUpdates() {
        try {
            if (!configuration.isNotification()) return;
            Scheduler.runTaskTimerAsync(() -> {
                new UpdateChecker(112942).getVersion(v -> {
                    if (v.equals(getPluginVersion())) return;
                    BoostedAudioAPI.api.info("§aNew version available : §6" + v + " §ayou are on §7" + getPluginVersion());
                });
            }, 0, 20 * 60 * 60);

            new TemporaryListener<PlayerJoinEvent>(PlayerJoinEvent.class, EventPriority.NORMAL, event -> {
                Player p = event.getPlayer();
                if (p.hasPermission("boostedaudio.admin")) {
                    new UpdateChecker(112747).getVersion(v -> {
                        if (v.equals(getPluginVersion())) return;
                        p.sendMessage("§2[BoostedAudio] §aNew version available : §e" + v + " §ayou are on §e" + getPluginVersion());
                    });
                    return true;
                }
                return false;
            });
        } catch (Exception ignored) {
        }
    }

    public String getPluginVersion() {
        return getDescription().getVersion();
    }


    public double getBukkitVersion() {
        try {
            NumberFormat f = NumberFormat.getInstance();
            return f.parse(Bukkit.getBukkitVersion()).doubleValue();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendServerPacket(String channel, String value) {
        DiffuserWebSocketClient ws = getInstance().diffuserWebSocketClient;
        if (ws != null && ws.isOpen()) ws.send(channel + ";" + value);
    }

    public static void registerServerPacketListener(String channel, ServerPacketListener listener) {
        DiffuserWebSocketClient.registerListener(channel, listener);
    }

}