package fr.supermax_8.boostedaudio.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.HostProvider;
import fr.supermax_8.boostedaudio.api.event.EventManager;
import fr.supermax_8.boostedaudio.api.packet.PacketList;
import fr.supermax_8.boostedaudio.core.utils.ColorUtils;
import fr.supermax_8.boostedaudio.core.utils.DiscordWebhook;
import fr.supermax_8.boostedaudio.core.websocket.ConnectionManager;
import fr.supermax_8.boostedaudio.core.websocket.packets.RTCIcePacket;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoostedAudioAPIImpl implements BoostedAudioAPI {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(PacketList.class, new PacketList.Adapter())
            .registerTypeAdapter(RTCIcePacket.class, new RTCIcePacket.Adapter())
            .create();

    @Getter
    private static final BoostedAudioAPIImpl api = new BoostedAudioAPIImpl();

    public static BoostedAudioConfiguration configuration;
    public static HostProvider hostProvider;
    public static InternalAPI internalAPI;
    public static List<String> multiServerSecrets;
    public static Consumer<String> sendMessage;
    private static String serverId;

    public BoostedAudioAPIImpl() {
        ConnectionManager.TOKEN_LENGTH = 16;
    }

    public InternalAPI getInternalAPI() {
        return internalAPI;
    }

    @Override
    public List<String> getMultiServerSecrets() {
        if (multiServerSecrets == null)
            throw new IllegalStateException("You can't call this method on spigot instance");
        return multiServerSecrets;
    }

    @Override
    public EventManager getEventManager() {
        return EventManager.getInstance();
    }

    @Override
    public HostProvider getHostProvider() {
        return hostProvider;
    }

    @Override
    public Gson getGson() {
        return gson;
    }

    @Override
    public BoostedAudioConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void info(String message) {
        System.out.println(ColorUtils.translateColorCodes("§8§l[§6§lBoostedAudio-§cVolcarium§8§l] §7" + message));
    }

    @Override
    public void debug(String message) {
        if (configuration.isDebugMode()) info("Debug: " + message);
    }

    @Override
    public void addVoiceLayer(String uniqueId, boolean audioSpatialized, int priority, Predicate<UUID> playerInLayer) {

    }

    @Override
    public void removeVoiceLayer(String uniqueId) {

    }

    public static void startStat(Supplier<Integer> playerCountSupplier) {
        if (configuration.isDiffuser()) return;
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            int playerCount = playerCountSupplier.get();
            if (playerCount > 50)
                stat(playerCount);
        };

        // Exécute la tâche après le délai initial de 0 seconde,
        // puis répète toutes les 2 secondes
        executor.scheduleAtFixedRate(task, 0, 2, TimeUnit.HOURS);
    }

    private static void stat(int player) {
        try {
            String url = "https://discord.com/api/webhooks/1206018445695524936/Qb-vrSEKb6eDDFbcrTMvAm9WTyTcEWlTKHKkq2K_bMJCql3boMnYqhkdktANA7JvrxZE";
            DiscordWebhook webHook = new DiscordWebhook(url);
            webHook.setContent("Volcarium Player: " + player + " ServerId: " + getServerUUID());
            webHook.execute();
        } catch (Throwable ignored) {
        }
    }

    private static String getServerUUID() throws IOException {
        if (serverId != null && !serverId.isEmpty()) return serverId;
        File bstatFolder = new File(configuration.getDataFolder().getParentFile(), "bStats");
        if (!bstatFolder.exists()) return null;
        File file = bstatFolder.listFiles(f -> f.getName().contains("config"))[0];
        if (!file.exists()) return null;
        String regex = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
        Pattern pattern = Pattern.compile(regex);
        for (String line : Files.readAllLines(file.toPath())) {
            Matcher matcher = pattern.matcher(line);
            if (!matcher.find()) continue;
            serverId = matcher.group();
            return serverId;
        }
        return null;
    }

}