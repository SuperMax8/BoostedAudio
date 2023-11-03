package fr.supermax_8.boostedaudio.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.HostProvider;
import fr.supermax_8.boostedaudio.core.websocket.PacketList;
import fr.supermax_8.boostedaudio.core.websocket.packets.RTCIcePacket;

public class BoostedAudioAPIImpl implements BoostedAudioAPI {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(PacketList.class, new PacketList.Adapter())
            .registerTypeAdapter(RTCIcePacket.class, new RTCIcePacket.Adapter())
            .create();

    private static final BoostedAudioAPIImpl api = new BoostedAudioAPIImpl();

    public static BoostedAudioConfiguration configuration;
    public static HostProvider hostProvider;
    public static InternalAPI internalAPI;


    public static BoostedAudioAPIImpl getApi() {
        return api;
    }

    public InternalAPI getInternalAPI() {
        return internalAPI;
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
        String finalMessage = "§8§l[§9§lBoostedAudio§8§l] §7" + message;
        System.out.println(finalMessage);
    }

    @Override
    public void debug(String message) {
        if (configuration.isDebugMode()) info("BoostedAudioDebug: " + message);
    }

}