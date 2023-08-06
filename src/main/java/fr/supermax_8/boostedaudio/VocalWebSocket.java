package fr.supermax_8.boostedaudio;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint("/")
public class VocalWebSocket {

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("Open");
    }

    @OnMessage
    public void onMessage(ByteBuffer audioData, Session session) {
        byte[] bytes = new byte[audioData.remaining()];
        audioData.get(bytes);

        // Traitez l'audioData reçu (par exemple, le diffuser à tous les autres clients connectés)
        System.out.println("Sending...");
        for (Session s : sessions) {
            // if (s.equals(session)) continue;
            try {
                s.getBasicRemote().sendBinary(ByteBuffer.wrap(bytes));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("Closed");
    }

    @OnError
    public void onError(Throwable t) {
        t.printStackTrace();
    }


}
