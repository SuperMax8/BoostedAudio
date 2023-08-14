package fr.supermax_8.boostedaudio.web.packets;

import fr.supermax_8.boostedaudio.web.ClientWebSocket;
import fr.supermax_8.boostedaudio.web.Packet;
import fr.supermax_8.boostedaudio.web.User;
import org.eclipse.jetty.websocket.api.Session;

import java.util.UUID;

public class TrustPacket implements Packet {

    private final String token;

    public TrustPacket(String token) {
        this.token = token;
    }

    @Override
    public void onReceive(User user) {
        UUID playerId;
        if ((playerId = ClientWebSocket.manager.getPlayerTokens().inverse().get(token)) != null)
            ClientWebSocket.manager.getUsers().put(playerId, new User(user.getSession(), token, playerId));
        else
            user.getSession().close();
    }


}