package fr.supermax_8.boostedaudio.web;

import fr.supermax_8.boostedaudio.Main;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class User {

    private final List<Peer> remotePeers = new ArrayList<>();
    private final Session session;
    private final String connectionToken;

    private final UUID playerId;

    /**
     * Use only this constructor if you only want to use the hashcode method of this class
     *
     * @param session The session
     */
    public User(Session session) {
        this.session = session;
        connectionToken = null;
        playerId = null;
    }

    public User(Session session, String connectionToken, UUID playerId) {
        this.session = session;
        this.connectionToken = connectionToken;
        this.playerId = playerId;
    }


    public List<Peer> getRemotePeers() {
        return remotePeers;
    }

    public Session getSession() {
        return session;
    }

    public String getConnectionToken() {
        return connectionToken;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    @Override
    public int hashCode() {
        return session.hashCode();
    }

    public void send(Packet... packets) {
        send(new PacketList(packets));
    }

    public void send(PacketList packetList) {
        String packet = Main.getGson().toJson(packetList);
        send(packet);
    }

    public void send(String packet) {
        try {
            session.getRemote().sendString(packet);
        } catch (IOException e) {
            session.close();
        }
    }

    public record Peer(UUID playerId) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Peer peer = (Peer) o;
            return Objects.equals(playerId, peer.playerId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(playerId);
        }

    }

}