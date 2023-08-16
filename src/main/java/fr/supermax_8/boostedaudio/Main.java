package fr.supermax_8.boostedaudio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.PacketList;
import fr.supermax_8.boostedaudio.web.User;
import fr.supermax_8.boostedaudio.web.packets.RTCIcePacket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(PacketList.class, new PacketList.Adapter())
            .registerTypeAdapter(RTCIcePacket.class, new RTCIcePacket.Adapter())
            .create();

    public static void main(String[] args) {
        int port = 8081;

        AudioWebSocketServer server = new AudioWebSocketServer(new InetSocketAddress("pyritemc.fr", port));

        scheduleWithInterval(() -> {
            try {
                List<User> users = new ArrayList<>(server.manager.getUsers().values());
                server.manager.linkPeers(users.get(0), users.get(1));
                System.out.println("Linking...");
            } catch (Exception e) {
                System.out.println("Not Linking ;(");
            }
        }, 5000);

        // Démarrer le serveur
        server.run();
        System.out.println("C ok mon frew: C, en ligne");
    }

    public static Gson getGson() {
        return gson;
    }

    public static void scheduleWithInterval(Runnable action, long intervalInMillis) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(action, 0, intervalInMillis, TimeUnit.MILLISECONDS);
    }

}