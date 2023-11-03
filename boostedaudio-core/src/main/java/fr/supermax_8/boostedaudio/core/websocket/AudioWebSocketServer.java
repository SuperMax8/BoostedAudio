package fr.supermax_8.boostedaudio.core.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.Packet;
/*import fr.supermax_8.boostedaudio.core.BoostedAudioHost;*/
import fr.supermax_8.boostedaudio.core.proximitychat.PeerConnection;
import fr.supermax_8.boostedaudio.core.websocket.packets.TrustPacket;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class AudioWebSocketServer extends WebSocketServer {

    private static AudioWebSocketServer instance;

    public final ConnectionManager manager = new ConnectionManager();
    private boolean isOpen = false;
    private static final Gson gson = BoostedAudioAPI.api.getGson();

    public AudioWebSocketServer(InetSocketAddress address) {
        super(address);
        instance = this;
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
            manager.sessionUsers.put(webSocket, Optional.empty());
            BoostedAudioAPI.api.debug("New connection WebSocket : " + webSocket.getRemoteSocketAddress().getAddress() + " / " + manager.getSessionUsers().size());
        } catch (Exception e) {
            if (BoostedAudioAPI.api.getConfiguration().isDebugMode()) e.printStackTrace();
        }
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        try {
            BoostedAudioAPI.api.debug("WebSocket connection closed : " + webSocket.getRemoteSocketAddress().getAddress());
            BoostedAudioAPI.api.debug("REASON " + s + " STATUSCODE: " + i);

            Optional<HostUser> user = manager.getSessionUsers().remove(webSocket);
            BoostedAudioAPI.api.debug("SessionUsersSize " + manager.getSessionUsers().size());
            if (user.isEmpty()) return;

            UUID playerId = user.get().getPlayerId();
            HostUser realUser = (HostUser) manager.getUsers().remove(playerId);

            if (realUser == null) return;

            for (Map.Entry<String, Set<UUID>> entry : realUser.getRemotePeers().entrySet()) {
                String layerId = entry.getKey();

                for (UUID id : entry.getValue()) {
                    HostUser usr = (HostUser) manager.getUsers().get(id);
                    if (usr == null) continue;
                    new PeerConnection(realUser.getPlayerId(), usr.getPlayerId(), layerId).unLink();
                }
            }
        } catch (Exception e) {
            if (BoostedAudioAPI.api.getConfiguration().isDebugMode()) e.printStackTrace();
        }
    }

    @Override
    public void onMessage(WebSocket client, String message) {
        PacketList packetList;
        try {
            packetList = gson.fromJson(message, PacketList.class);
        } catch (JsonSyntaxException e) {
            client.close();
            BoostedAudioAPI.api.debug("ERREUR RECEIVED MESSAGE");
            BoostedAudioAPI.api.debug(message);
            return;
        } catch (Exception e) {
            if (BoostedAudioAPI.api.getConfiguration().isDebugMode()) e.printStackTrace();
            BoostedAudioAPI.api.debug(message);
            return;
        }

        Optional<HostUser> user = manager.getSessionUsers().get(client);
        if (user.isPresent())
            for (Packet packet : packetList.getPackets()) packet.onReceive(user.get(), this);
        else
            testToTrust(client, packetList);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        BoostedAudioAPI.api.debug("ERRRRREEEEUR WEBSOCKET " + webSocket.getRemoteSocketAddress());
        BoostedAudioAPI.api.debug(e.toString());
        if (BoostedAudioAPI.api.getConfiguration().isDebugMode()) e.printStackTrace();
    }

    @Override
    public void onStart() {
        BoostedAudioAPI.api.debug("WebSocketServer Open");
        isOpen = true;
        setConnectionLostTimeout(10);
    }

    private void testToTrust(WebSocket session, PacketList message) {
        Packet packet;
        if (message.getPackets().isEmpty() || !((packet = message.getPackets().get(0)) instanceof TrustPacket)) {
            BoostedAudioAPI.api.debug("Kick Untrust: " + session);
            session.close();
        } else packet.onReceive(new HostUser(session, null, null), this);
    }


    public boolean isOpen() {
        return isOpen;
    }


    public static AudioWebSocketServer getInstance() {
        return instance;
    }


}