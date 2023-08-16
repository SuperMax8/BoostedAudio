package fr.supermax_8.boostedaudio.web;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fr.supermax_8.boostedaudio.Main;
import fr.supermax_8.boostedaudio.web.packets.TrustPacket;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class AudioWebSocketServer extends WebSocketServer {

    public final ConnectionManager manager = new ConnectionManager();

    private static final Gson gson = Main.getGson();

    public AudioWebSocketServer(InetSocketAddress address) {
        super(address);
    }


/*    public static void broadcast(Session sender, Packet... packets) {
        Collection<Session> c = new LinkedList<>(manager.sessionUsers.values());
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
    }*/

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        manager.sessionUsers.put(webSocket, null);

        System.out.println("Nouvelle connexion WebSocket : " + webSocket.getRemoteSocketAddress().getAddress() + " / " + manager.getSessionUsers().size());
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        System.out.println("Connexion WebSocket fermée : " + webSocket.getRemoteSocketAddress().getAddress());
        System.out.println("REASON " + s + " STATUSCODE: " + i);
        User user = manager.getSessionUsers().remove(webSocket);
        manager.getUsers().remove(user.getPlayerId());
        //if (playerID != null) manager.playerTokens.remove(playerID);
    }

    @Override
    public void onMessage(WebSocket client, String message) {
        System.out.println("Message RECEIVED: " + message);
        PacketList packetList;
        try {
            packetList = gson.fromJson(message, PacketList.class);
        } catch (JsonSyntaxException e) {
            client.close();
            return;
        }

        User user = manager.getSessionUsers().get(client);
        if (user != null)
            for (Packet packet : packetList.getPackets()) packet.onReceive(user, this);
        else
            testToTrust(client, packetList);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocketServer Open");
    }

    private void testToTrust(WebSocket session, PacketList message) {
        Packet packet;
        if (message.getPackets().isEmpty() || !((packet = message.getPackets().get(0)) instanceof TrustPacket)) {
            System.out.println("Kick Untrust: " + session);
            session.close();
        } else packet.onReceive(new User(session), this);
    }

}