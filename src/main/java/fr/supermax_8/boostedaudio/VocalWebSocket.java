package fr.supermax_8.boostedaudio;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint("/audio")
public class VocalWebSocket {


        private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

        @OnOpen
        public void onOpen(Session session) {
            sessions.add(session);
        }

        @OnMessage
        public void onMessage(byte[] audioData, Session session) {
            // Traitez l'audioData reçu (par exemple, le diffuser à tous les autres clients connectés)
            for (Session s : sessions) {
                if (!s.equals(session)) {
                    try {
                        s.getBasicRemote().sendBinary(ByteBuffer.wrap(audioData));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @OnClose
        public void onClose(Session session) {
            sessions.remove(session);
        }

        @OnError
        public void onError(Throwable t) {
            t.printStackTrace();
        }


}
