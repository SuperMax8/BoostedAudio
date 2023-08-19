package fr.supermax_8.boostedaudio.web;

import fr.supermax_8.boostedaudio.BoostedAudio;
import org.java_websocket.WebSocket;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class User {

    private final Set<UUID> remotePeers = new HashSet<>();
    private final WebSocket session;
    private final String connectionToken;
    private final UUID playerId;

    /**
     * Use only this constructor if you only want to use the hashcode method of this class
     *
     * @param session The sessionq
     */
    public User(WebSocket session) {
        this.session = session;
        connectionToken = null;
        playerId = null;
    }

    public User(WebSocket session, String connectionToken, UUID playerId) {
        this.session = session;
        this.connectionToken = connectionToken;
        this.playerId = playerId;
    }


    public Set<UUID> getRemotePeers() {
        return remotePeers;
    }

    public WebSocket getSession() {
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
        String packet = BoostedAudio.getGson().toJson(packetList);
        send(packet);
    }

    public void send(String packet) {
        if (session.isOpen()) session.send(packet);
    }

}