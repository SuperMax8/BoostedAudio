package fr.supermax_8.boostedaudio.web;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fr.supermax_8.boostedaudio.Main;
import fr.supermax_8.boostedaudio.web.packets.TrustPacket;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@WebSocket
public class ClientWebSocket extends WebSocketServlet {


    public static final ConnectionManager manager = new ConnectionManager();

    private static final Gson gson = Main.getGson();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        manager.sessions.add(session);
        session.setIdleTimeout(0);
        System.out.println("Nouvelle connexion WebSocket : " + session.getRemoteAddress().getAddress() + " / " + manager.getSessions().size());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        PacketList packetList;
        try {
            packetList = gson.fromJson(message, PacketList.class);
        } catch (JsonSyntaxException e) {
            session.close();
            return;
        }

        UUID id;
        if ((id = manager.getUsers().inverse().get(new User(session))) != null)
            for (Packet packet : packetList.getPackets()) packet.onReceive(manager.users.get(id));
        else
            testToTrust(session, packetList);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("Connexion WebSocket fermée : " + session.getRemoteAddress().getAddress());
        manager.getSessions().remove(session);
        UUID playerID = manager.users.inverse().remove(new User(session));
        if (playerID != null) manager.playerTokens.remove(playerID);
    }

/*    @OnWebSocketError
    public void onError(Session session) {

    }*/

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(ClientWebSocket.class);
    }

    private static void testToTrust(Session session, PacketList message) {
        Packet packet;
        if (message.getPackets().isEmpty() || !((packet = message.getPackets().get(0)) instanceof TrustPacket))
            session.close();
        else packet.onReceive(new User(session));
    }

    public static void broadcast(Session sender, Packet... packets) {
        Collection<Session> c = new LinkedList<>(manager.sessions);
        c.remove(sender);
        broadcast(c, packets);
    }

    public static void broadcast(Collection<Session> sessions, Packet... packets) {
        PacketList list = new PacketList(List.of(packets));
        String packet = gson.toJson(list);

        try {
            for (Session s : sessions) s.getRemote().sendString(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}