package fr.supermax_8.boostedaudio;

import org.glassfish.tyrus.server.Server;

public class Main {

    private static final int PORT = 8080;

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
    }


}