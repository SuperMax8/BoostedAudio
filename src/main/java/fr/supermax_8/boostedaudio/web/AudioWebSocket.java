package fr.supermax_8.boostedaudio.web;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.ingame.AudioManager;
import fr.supermax_8.boostedaudio.web.packets.TrustPacket;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class AudioWebSocket {

    public final ConnectionManager manager = BoostedAudio.getInstance().getConnectionManager();
    private static final Gson gson = BoostedAudio.getGson();

    public AudioWebSocket() {
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

    public void onOpen(Session session) {
        try {
            manager.sessionUsers.put(session, Optional.empty());
            BoostedAudio.debug("New connection WebSocket : " + session.getIp() + " / " + manager.getSessionUsers().size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClose(Session session) {
        try {
            BoostedAudio.debug("WebSocket connection closed : " + session.getIp());
            //BoostedAudio.debug("REASON " + s + " STATUSCODE: " + i);

            Optional<User> user = manager.getSessionUsers().remove(session);
            BoostedAudio.debug("SessionUsersSize " + manager.getSessionUsers().size());
            if (!user.isPresent()) return;

            UUID playerId = user.get().getPlayerId();
            User realUser = manager.getUsers().remove(playerId);

            for (UUID id : realUser.getRemotePeers()) {
                User usr = manager.getUsers().get(id);
                new AudioManager.PeerConnection(realUser.getPlayerId(), usr.getPlayerId()).unLink();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onMessage(String message, Session session) throws IOException {
        PacketList packetList;
        try {
            packetList = gson.fromJson(message, PacketList.class);
        } catch (JsonSyntaxException e) {
            session.close();
            BoostedAudio.debug("ERREUR RECEIVED MESSAGE");
            BoostedAudio.debug(message);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            BoostedAudio.debug(message);
            return;
        }

        Optional<User> user = manager.getSessionUsers().get(session);
        if (user.isPresent())
            for (Packet packet : packetList.getPackets()) packet.onReceive(user.get(), this);
        else
            testToTrust(session, packetList);
    }

    private void testToTrust(Session session, PacketList message) {
        Packet packet;
        if (message.getPackets().isEmpty() || !((packet = message.getPackets().get(0)) instanceof TrustPacket)) {
            BoostedAudio.debug("Kick Untrust: " + session);
            session.close();
        } else packet.onReceive(new User(session, null, null), this);
    }

}