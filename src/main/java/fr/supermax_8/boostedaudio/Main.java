package fr.supermax_8.boostedaudio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.supermax_8.boostedaudio.web.ClientWebSocket;
import fr.supermax_8.boostedaudio.web.PacketList;
import fr.supermax_8.boostedaudio.web.packets.RTCIcePacket;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Main {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(PacketList.class, new PacketList.Adapter())
            .registerTypeAdapter(RTCIcePacket.class, new RTCIcePacket.Adapter())
            .create();

    /*private static final int PORT = 8080;

    public static void main(String[] args) {
        Server server = new Server("127.0.0.1", PORT, "/", null, VocalWebSocket.class);

        try {
            server.start();
            System.out.println("Serveur WebSocket démarré");
            System.out.println("Appuyez sur Enter pour arrêter le serveur.");
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }*/

    public static void main(String[] args) throws Exception {
//        /*System.out.println(gson.toJson(new RTCSessionDescriptionPacket("aa", "bb")));
//
//        if (true) return;*/
        int port = 8081; // Port sur lequel le serveur écoutera les connexions WebSocket
        Server server = new Server(port);

        // Créer un gestionnaire de contexte de servlet pour le serveur
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Ajouter la classe WebRTCSocket comme gestionnaire WebSocket à l'URL spécifique
        context.addServlet(new ServletHolder(new ClientWebSocket()), "/");

        // Démarrer le serveur
        server.start();
        System.out.println("C ok mon frew: C, en ligne");
        server.join();
    }

    public static Gson getGson() {
        return gson;
    }

}