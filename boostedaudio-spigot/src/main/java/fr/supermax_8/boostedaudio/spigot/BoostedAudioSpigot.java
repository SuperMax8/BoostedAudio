package fr.supermax_8.boostedaudio.spigot;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.HostProvider;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.core.*;
import fr.supermax_8.boostedaudio.core.pluginmessage.UsersFromUuids;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceChatManager;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceChatResult;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceLayer;
import fr.supermax_8.boostedaudio.core.utils.UpdateChecker;
import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfiguration;
import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfigurationSection;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;
import fr.supermax_8.boostedaudio.spigot.commands.AudioCommand;
import fr.supermax_8.boostedaudio.spigot.proximitychat.VoiceChatProcessor;
import fr.supermax_8.boostedaudio.spigot.utils.Scheduler;
import fr.supermax_8.boostedaudio.spigot.utils.TemporaryListener;
import fr.supermax_8.boostedaudio.spigot.utils.configuration.SpigotCrossConfiguration;
import fr.supermax_8.boostedaudio.spigot.utils.configuration.SpigotCrossConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.wildfly.common.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

public final class BoostedAudioSpigot extends JavaPlugin {

    private static BoostedAudioSpigot instance;

    @Nullable
    private BoostedAudioHost host;
    @Nullable
    private VoiceChatManager voiceChatManager;

    @Nullable
    private HostRequester hostRequester;
    @Nullable
    private Map<UUID, User> usersOnServer;

    private VoiceChatProcessor voiceChatProcessor;
    private BoostedAudioConfiguration configuration;

    @Override
    public void onEnable() {
        instance = this;

        CrossConfiguration.instancer = SpigotCrossConfiguration::new;
        CrossConfigurationSection.converter = o -> new SpigotCrossConfigurationSection((ConfigurationSection) o);
        configuration = new BoostedAudioConfiguration(new File(getDataFolder(), "config.yml"));
        BoostedAudioAPIImpl.configuration = configuration;
        getCommand("audio").setExecutor(new AudioCommand());

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

        if (configuration.isBungeecoord()) setupDiffuser();
        else setupHost();

        voiceChatProcessor = new VoiceChatProcessor();
        voiceChatProcessor.getLayers().put("proximitychat", new VoiceLayer(true, 0, null, "proximitychat"));
    }

    @Override
    public void onDisable() {

    }

    public static BoostedAudioSpigot getInstance() {
        return instance;
    }

    private void initMetrics() {
        int pluginId = 19857;
     /*   Metrics metrics = new Metrics(BoostedAudioSpigot.getInstance(), pluginId);
        metrics.addCustomChart(new Metrics.SimplePie("sucessful_setup", () -> String.valueOf(host.isSucessfulSetup())));
        metrics.addCustomChart(new Metrics.SimplePie("ffmpeg_setuped", () -> String.valueOf(FileUtils.ffmpeg != null)));
        metrics.addCustomChart(new Metrics.SingleLineChart("players_connected_to_audio_panel", () ->
                host.getWebSocketServer().manager.getUsers().size()));*/
        //metrics.addCustomChart(new Metrics.SimplePie("nbspeakers", () -> DataVisualisationUtils.intMetricToEzReadString(BoostedAudioHost.getInstance().getAudioManager().getSpeakerManager().speakers.size())));
        /*metrics.addCustomChart(new Metrics.SimplePie("nbregions", () -> {
            try {
                return DataVisualisationUtils.intMetricToEzReadString(BoostedAudioHost.getInstance().getAudioManager().getRegionManager().getAudioRegions().size());
            } catch (Exception e) {
                return "No regions";
            }
        }));*/
    }

    private void setupHost() {
        // Host mode
        host = new BoostedAudioHost(configuration);
        Bukkit.getScheduler().runTaskLater(this, this::initMetrics, 20 * 60);
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
                if (configuration.isDebugMode()) {
                    System.out.println("Result : " + BoostedAudioAPI.api.getGson().toJson(result));
                }
                voiceChatManager.processResult(result);
            } catch (Throwable e) {
                System.out.println("Error while processing voice chat :");
                e.printStackTrace();
            }
        }, 0, 60);
    }

    private void setupDiffuser() {
        // Diffuser mode
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
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!Requesting users!!!!!!!!!!!!!!!!!!!!!!!!");
                StringJoiner joiner = new StringJoiner(";");
                Bukkit.getOnlinePlayers().forEach(player -> joiner.add(player.getUniqueId().toString()));
                hostRequester.request("usersfromuuids", joiner.toString(), (UsersFromUuids usersFromUuids) -> {
                    Map<UUID, User> map = new HashMap<>();
                    usersFromUuids.getUsers().forEach(user -> map.put(user.getPlayerId(), new DiffuserUser(user)));
                    usersOnServer = map;
                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!SETTTTTINGGG users!!!!!!!!!!!!!!!!!!!!!!!!");
                }, UsersFromUuids.class);
                usersOnServer = null;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }, 0, 0);


        registerOutgoingPluginMessage("tick");
        registerOutgoingPluginMessage("audiotoken");
        registerIncomingPluginMessage("audiotoken", (channel, player, message) -> {
            String msg = new String(message);
            String[] split = msg.split(";", 2);
            OfflinePlayer player1 = Bukkit.getOfflinePlayer(UUID.fromString(split[0]));
            if (!player1.isOnline()) return;
            AudioCommand.sendConnectMessage(player1.getPlayer(), split[1]);
        });

        Scheduler.runTaskTimerAsync(() -> {
            try {
                VoiceChatResult result = voiceChatProcessor.process();
                if (result == null) return;
                sendPluginMessage("tick", BoostedAudioAPI.api.getGson().toJson(result));
            } catch (Throwable e) {
                System.out.println("Error while processing voice chat :");
                e.printStackTrace();
            }
        }, 0, 0);


/*        Scheduler.runTaskTimer(() -> {
            Bukkit.getServer().sendPluginMessage(this, "boostedaudio:fromspigot", "From spigot messaaaaaage !!!!!!!".getBytes());
            //Bukkit.getOnlinePlayers().stream().findFirst().get().sendPluginMessage(this, "boostedaudio:fromspigot", "From spigot messaaaaaage !!!!!!!".getBytes());
            //Bukkit.getServer().getMessenger().dispatchIncomingMessage(Bukkit.getOnlinePlayers().stream().findFirst().get(), "boostedaudio:fromspigot", "From spigot messaaaaaage !!!!!!!".getBytes());
        }, 0, 40);*/
    }


    private void checkForUpdates() {
        try {
            if (!configuration.isNotification()) return;
            BoostedAudioAPI.api.info("Checking for updates...");
            new UpdateChecker(112747).getVersion(v -> {
                if (v.equals(getPluginVersion())) return;
                Scheduler.runTask(() -> {
                    BoostedAudioAPI.api.info("§aNew version available : §6" + v + " §ayou are on §7" + getPluginVersion());
                    new TemporaryListener<PlayerJoinEvent>(PlayerJoinEvent.class, EventPriority.NORMAL, event -> {
                        Player p = event.getPlayer();
                        if (p.hasPermission("boostedaudio.admin")) {
                            p.sendMessage("§2[BoostedAudio] §aNew version available : §e" + v + " §ayou are on §e" + getPluginVersion());
                            return true;
                        }
                        return false;
                    });
                });
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


    public static void sendPluginMessage(String channel, String value) {
        Bukkit.getServer().sendPluginMessage(instance, "boostedaudio:" + channel, value.getBytes());
        System.out.println("Sending message to " + channel + " : " + value);
    }

    public static void registerIncomingPluginMessage(String channel, PluginMessageListener listener) {
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(instance, "boostedaudio:" + channel, listener);
    }

    public static void registerOutgoingPluginMessage(String channel) {
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(instance, "boostedaudio:" + channel);
    }

}