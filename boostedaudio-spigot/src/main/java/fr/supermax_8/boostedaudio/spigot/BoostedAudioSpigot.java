package fr.supermax_8.boostedaudio.spigot;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.ServerImplementation;
import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.HostProvider;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.core.*;
import fr.supermax_8.boostedaudio.core.multiserv.DiffuserWebSocketClient;
import fr.supermax_8.boostedaudio.core.multiserv.ServerPacketListener;
import fr.supermax_8.boostedaudio.core.multiserv.UsersFromUuids;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceChatManager;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceLayer;
import fr.supermax_8.boostedaudio.core.utils.DataVisualisationUtils;
import fr.supermax_8.boostedaudio.core.utils.Lang;
import fr.supermax_8.boostedaudio.core.utils.MediaDownloader;
import fr.supermax_8.boostedaudio.core.utils.UpdateChecker;
import fr.supermax_8.boostedaudio.spigot.commands.AudioCommandSpigot;
import fr.supermax_8.boostedaudio.spigot.commands.AudioQRcodeCommand;
import fr.supermax_8.boostedaudio.spigot.commands.BoostedAudioCommand;
import fr.supermax_8.boostedaudio.spigot.commands.MuteCommand;
import fr.supermax_8.boostedaudio.spigot.diffuser.DiffuserUser;
import fr.supermax_8.boostedaudio.spigot.manager.AudioManager;
import fr.supermax_8.boostedaudio.spigot.manager.PlaceHoldersManager;
import fr.supermax_8.boostedaudio.spigot.manager.RegionManager;
import fr.supermax_8.boostedaudio.spigot.proximitychat.VoiceChatProcessor;
import fr.supermax_8.boostedaudio.spigot.utils.AroundManager;
import fr.supermax_8.boostedaudio.spigot.utils.FileUtils;
import fr.supermax_8.boostedaudio.spigot.utils.TemporaryListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.wildfly.common.annotation.Nullable;

import javax.swing.plaf.synth.Region;
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
import java.util.function.Consumer;

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
    @Getter
    private final FoliaLib folia = new FoliaLib(this);
    @Getter
    private final ServerImplementation scheduler = folia.getImpl();

    @Override
    public void onEnable() {
        instance = this;
        usersOnServer = new ConcurrentHashMap<>();
        BoostedAudioAPIImpl.sendMessage = s -> Bukkit.getConsoleSender().sendMessage(s);

        configuration = new BoostedAudioConfiguration(new File(getDataFolder(), "config.yml"));
        BoostedAudioAPIImpl.configuration = configuration;

        Lang.init(getDataFolder());

        getCommand("audio").setExecutor(new AudioCommandSpigot());
        getCommand("boostedaudio").setExecutor(new BoostedAudioCommand());
        getCommand("audioqrcode").setExecutor(new AudioQRcodeCommand());
        getCommand("mute").setExecutor(new MuteCommand());

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
        scheduler.runTimerAsync(() -> aroundManager.run(), 0, 1);

        // Placeholders
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceHolderAPI")) {
            new PlaceHoldersManager().register();
            BoostedAudioAPI.getAPI().info("Placeholders loaded successfully");
        } else BoostedAudioAPI.getAPI().info("PlaceholderAPI is not on the server");

        scheduler.runAsync(task -> {
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
                scheduler.runTimerAsync(() -> {
                    try {
                        regionManager.tick(BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer());
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }, 0, 0);

            // Plugin reload handle
            for (Player p : Bukkit.getOnlinePlayers())
                new PlayerListener().join(new PlayerJoinEvent(p, ""));
        });

        voiceChatProcessor = new VoiceChatProcessor();
        if (configuration.isVoiceChatEnabled())
            voiceChatProcessor.getLayers().put("proximitychat", new VoiceLayer(true, 0, null, "proximitychat"));

        scheduler.runLaterAsync(this::initMetrics, 20 * 60);
        BoostedAudioAPIImpl.startStat(() -> Bukkit.getOnlinePlayers().size());
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
        scheduler.runTimerAsync(() -> {
            try {
                voiceChatProcessor.process(result -> voiceChatManager.processResult(result));
            } catch (Throwable e) {
                BoostedAudioAPI.getAPI().info("Error while processing voice chat :");
                e.printStackTrace();
            }
        }, 20, 1);
    }

    private void setupDiffuser() {
        // Diffuser mode
        workingMode = "Diffuser";

        // Init
        scheduler.runAsync(task -> {
            // Init DiffuserWebSocketClient when a player is online to avoid pluginChannel problems
            BoostedAudioAPI.getAPI().debug("Bungee websocket uri: " + configuration.getMainProxyWebsocketLink());

            scheduler.runTimerAsync(() -> {
                if (diffuserWebSocketClient != null && diffuserWebSocketClient.isConnected()) return;
                CompletableFuture.runAsync(() -> {
                    try {
                        if (diffuserWebSocketClient != null) diffuserWebSocketClient.close();
                        BoostedAudioAPI.getAPI().debug("Diffuser try to connect...");
                        hostRequester.clear();
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

            scheduler.runTimerAsync(() -> {
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
            }, 0, 1);
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

        scheduler.runTimerAsync(() -> {
            try {
                voiceChatProcessor.process(result -> sendServerPacket("tick", BoostedAudioAPI.api.getGson().toJson(result)));
            } catch (Throwable e) {
                BoostedAudioAPI.getAPI().info("Error while processing voice chat :");
                e.printStackTrace();
            }
        }, 0, 1);
    }


    private void checkForUpdates() {
        try {
            if (!configuration.isNotification()) return;
            scheduler.runTimerAsync(() -> {
                new UpdateChecker(112942).getVersion(v -> {
                    if (
                            Integer.getInteger(v.replaceAll("\\.", ""))
                                    <=
                                    Integer.getInteger(getPluginVersion().replaceAll("\\.", ""))
                    ) return;
                    BoostedAudioAPI.api.info("§aNew version available : §6" + v + " §ayou are on §7" + getPluginVersion());
                });
            }, 0, 20 * 60 * 60);

            new TemporaryListener<PlayerJoinEvent>(PlayerJoinEvent.class, EventPriority.NORMAL, event -> {
                Player p = event.getPlayer();
                if (p.hasPermission("boostedaudio.admin")) {
                    new UpdateChecker(112942).getVersion(v -> {
                        if (
                                Integer.getInteger(v.replaceAll("\\.", ""))
                                        <=
                                        Integer.getInteger(getPluginVersion().replaceAll("\\.", ""))
                        ) return;
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

    public static String getServerVersion() {
        Server server = Bukkit.getServer();
        return server.getVersion().toLowerCase();
    }

    public double getBukkitVersion() {
        try {
            NumberFormat f = NumberFormat.getInstance();
            return f.parse(Bukkit.getBukkitVersion()).doubleValue();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void downloadAudio(String mediaLink, Consumer<String> whenDownloadedNewLink) {
        if (instance.configuration.isDiffuser()) {
            instance.hostRequester.request("downloadaudio", mediaLink, whenDownloadedNewLink, String.class);
        } else {
            File dir = new File(instance.configuration.getDataFolder(), "webhost" + File.separator + "audio" + File.separator + "downloaded");
            String fileName = MediaDownloader.download(mediaLink, BoostedAudioAPI.getAPI().getConfiguration().getAudioDownloaderFormat(), dir);
            whenDownloadedNewLink.accept("audio/downloaded/" + fileName);
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