package fr.supermax_8.boostedaudio;

import org.glassfish.tyrus.server.Server;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@ServerEndpoint("/audio")
public class Main {


    private static List<Session> sessions = new ArrayList<>();

    @OnOpen
    public void onOpen(Session session) {
        // Ajouter la nouvelle session à la liste
        sessions.add(session);
        System.out.println("Nouvelle connexion WebSocket établie");
    }

    @OnMessage
    public void onMessage(ByteBuffer audioData, Session session) {
        // Relayer le flux audio à tous les autres clients connectés
        for (Session s : sessions) {
            if (!s.equals(session)) {
                try {
                    s.getBasicRemote().sendBinary(audioData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        // Supprimer la session de la liste lorsqu'elle se déconnecte
        sessions.remove(session);
        System.out.println("Connexion WebSocket fermée");
    }

    public static void main(String[] args) {
        Server server = new Server("localhost", 11000, "/audio", null, Main.class);
        try {
            server.start();
            System.out.println("Serveur WebSocket démarré");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }


    /*public static void main(String[] args) {
        startServer();
    }

    private static void startServer() {
        Server server = new Server("localhost", 8080, "/audio", null, VocalWebSocket.class);

        try {
            server.start();
            System.out.println("Serveur WebSocket démarré sur : " + server.getPort());
            System.out.println("En attente de connexions...");

            // Pour garder le serveur en cours d'exécution indéfiniment (jusqu'à ce qu'il soit arrêté manuellement)
            Thread.currentThread().join();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }*/

}
