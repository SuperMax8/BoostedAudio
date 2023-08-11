package fr.supermax_8.boostedaudio.websocket;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import fr.supermax_8.boostedaudio.Main;
import fr.supermax_8.boostedaudio.websocket.packets.TrustPacket;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import java.util.*;

@WebSocket
public class ClientWebSocket extends WebSocketServlet {


    /**
     * UUID: Minecraft player uuid
     * String: token
     */
    public static final BiMap<UUID, String> playerTokens = HashBiMap.create();

    /**
     * The users trusted with the token link to the minecraft client
     * String: The token gave by the server in game
     * Session: The websocket session
     */
    public static final BiMap<String, Session> trustedUsers = HashBiMap.create();
    public static final HashSet<Session> sessions = new HashSet<>();

    private final Gson gson = Main.getGson();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        sessions.add(session);
        session.setIdleTimeout(0);
        System.out.println("Nouvelle connexion WebSocket : " + session.getRemoteAddress().getAddress() + " / " + sessions.size());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        PacketList packetList = gson.fromJson(message, PacketList.class);
        if (trustedUsers.containsValue(session))
            for (Packet packet : packetList.getPackets()) packet.onReceive(session, this);
        else
            testToTrust(session, packetList);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("Connexion WebSocket fermée : " + session.getRemoteAddress().getAddress());
        sessions.remove(session);
        String token = trustedUsers.inverse().remove(session);
        if (token != null) playerTokens.inverse().remove(token);
    }

/*    @OnWebSocketError
    public void onError(Session session) {

    }*/

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(ClientWebSocket.class);
    }

    private void testToTrust(Session session, PacketList message) {
        Packet packet;
        if (message.getPackets().isEmpty() || !((packet = message.getPackets().get(0)) instanceof TrustPacket))
            session.close();
        else packet.onReceive(session, this);
    }

    public void sendPackets(Session sender, Packet... packets) {
        Collection<Session> c = new LinkedList<>(sessions);
        c.remove(sender);
        sendPackets(c, packets);
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


}