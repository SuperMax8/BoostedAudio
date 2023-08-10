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
import java.util.LinkedList;
import java.util.List;

@WebSocket
public class WebRTCSocket extends WebSocketServlet {

    private static final List<Session> sessions = new ArrayList<>();

    private final Gson gson = Main.getGson();

    public WebRTCSocket() {
        System.out.println("UWU");
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        sessions.add(session);
        session.setIdleTimeout(0);
        System.out.println("Nouvelle connexion WebSocket : " + session.getRemoteAddress().getAddress() + " / " + sessions.size());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        System.out.println("Message reçu du client :" + sessions.indexOf(session) + "/" + sessions.size());
        /*System.out.println(message);*/
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

    public void sendPackets(Session sender, Packet... packets) {
        System.out.println("SESS " + sessions.size());
        Collection<Session> c = new LinkedList<>(sessions);
        c.remove(sender);
        sendPackets(c, packets);
    }

    public void sendPackets(Collection<Session> sessions, Packet... packets) {
        PacketList list = new PacketList(List.of(packets));
        String packet = gson.toJson(list);
        System.out.println("Send:");
        for (Session sess : sessions) System.out.println("Sending to client: " + this.sessions.indexOf(sess));
        try {
            System.out.println("SENDING: " + packet);
            for (Session s : sessions) s.getRemote().sendString(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Session> getSessions() {
        return sessions;
    }

}