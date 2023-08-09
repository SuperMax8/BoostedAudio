package fr.supermax_8.boostedaudio;

import com.google.gson.Gson;
import fr.supermax_8.boostedaudio.websocket.Packet;
import fr.supermax_8.boostedaudio.websocket.PacketList;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@WebSocket
public class WebRTCSocket extends WebSocketServlet {

    private final List<Session> sessions = new ArrayList<>();

    private final Gson gson = Main.getGson();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Nouvelle connexion WebSocket : " + session.getRemoteAddress().getAddress());
        sessions.add(session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        System.out.println("Message reçu du client :" + sessions.indexOf(session));
        PacketList packetList = gson.fromJson(message, PacketList.class);

        for (Packet packet : packetList.getPackets()) packet.onReceive(session, this);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("Connexion WebSocket fermée : " + session.getRemoteAddress().getAddress());
        sessions.remove(session);
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(WebRTCSocket.class);
    }

    public void sendPackets(Collection<Session> sessions, Packet... packets) {
        PacketList list = new PacketList(List.of(packets));
        String packet = gson.toJson(list);
        try {
            for (Session s : sessions) s.getRemote().sendString(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Session> getSessions() {
        return sessions;
    }

}