package fr.supermax_8.boostedaudio.websocket.packets;

import fr.supermax_8.boostedaudio.websocket.ClientWebSocket;
import fr.supermax_8.boostedaudio.websocket.Packet;
import org.eclipse.jetty.websocket.api.Session;

public class TrustPacket implements Packet {

    private final String token;

    public TrustPacket(String token) {
        this.token = token;
    }

    @Override
    public void onReceive(Session session, ClientWebSocket socket) {
        if (ClientWebSocket.playerTokens.containsValue(token))
            ClientWebSocket.trustedUsers.put(token, session);
        else
            session.close();
    }


}