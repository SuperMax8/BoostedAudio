package fr.supermax_8.boostedaudio.web;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.web.packets.TrustPacket;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.UUID;

public class AudioWebSocketServer extends WebSocketServer {

    public final ConnectionManager manager = new ConnectionManager();
    private boolean isOpen = false;
    private static final Gson gson = BoostedAudio.getGson();

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
        try {
            manager.sessionUsers.put(webSocket, new User(null));
            BoostedAudio.debug("New connection WebSocket : " + webSocket.getRemoteSocketAddress().getAddress() + " / " + manager.getSessionUsers().size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        try {
            BoostedAudio.debug("WebSocket connection closed : " + webSocket.getRemoteSocketAddress().getAddress());
            BoostedAudio.debug("REASON " + s + " STATUSCODE: " + i);

            User user = manager.getSessionUsers().remove(webSocket);
            BoostedAudio.debug("SessionUsersSize " + manager.getSessionUsers().size());
            if (user == null) return;

            UUID playerId = user.getPlayerId();
            User realUser = manager.getUsers().remove(playerId);

            for (UUID id : realUser.getRemotePeers()) {
                User usr = manager.getUsers().get(id);
                manager.unlinkPeers(realUser, usr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(WebSocket client, String message) {
        PacketList packetList;
        try {
            packetList = gson.fromJson(message, PacketList.class);
        } catch (JsonSyntaxException e) {
            client.close();
            return;
        }

        User user = manager.getSessionUsers().get(client);
        if (user != null && user.getSession() != null)
            for (Packet packet : packetList.getPackets()) packet.onReceive(user, this);
        else
            testToTrust(client, packetList);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        BoostedAudio.debug("ERRRRREEEEUR WEBSOCKET " + webSocket.getRemoteSocketAddress());
        e.printStackTrace();
    }

    @Override
    public void onStart() {
        BoostedAudio.debug("WebSocketServer Open");
        isOpen = true;
    }

    private void testToTrust(WebSocket session, PacketList message) {
        Packet packet;
        if (message.getPackets().isEmpty() || !((packet = message.getPackets().get(0)) instanceof TrustPacket)) {
            BoostedAudio.debug("Kick Untrust: " + session);
            session.close();
        } else packet.onReceive(new User(session), this);
    }


    public boolean isOpen() {
        return isOpen;
    }
}