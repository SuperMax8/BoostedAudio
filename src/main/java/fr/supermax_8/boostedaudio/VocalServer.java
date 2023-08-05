/*
package org.example;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/vocal_channel")
public class VocalServer {

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("Nouvelle connexion : " + session.getId());
    }

    @OnMessage
    public void onMessage(ByteBuffer audioData, Session session) {
        try {
            // Diffuser les données audio à tous les clients connectés, à l'exception de l'émetteur
            for (Session s : sessions) {
                if (!s.equals(session)) {
                    s.getBasicRemote().sendBinary(audioData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        sessions.remove(session);
        System.out.println("Déconnexion : " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("Erreur de connexion pour : " + session.getId());
        error.printStackTrace();
    }
}*/
