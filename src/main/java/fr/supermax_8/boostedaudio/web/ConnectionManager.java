package fr.supermax_8.boostedaudio.web;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.web.packets.AddPeerPacket;
import fr.supermax_8.boostedaudio.web.packets.RemovePeerPacket;
import org.java_websocket.WebSocket;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    /**
     * UUID: Minecraft player uuid
     * String: token
     */
    protected final ConcurrentHashMap<UUID, String> playerTokens = new ConcurrentHashMap<>();

    /**
     * The users trusted with the token link to the minecraft client
     * String: The token gave by the server in game
     * Session: The websocket session
     */
    protected final ConcurrentHashMap<UUID, User> users = new ConcurrentHashMap<>();

    protected final ConcurrentHashMap<WebSocket, User> sessionUsers = new ConcurrentHashMap<>();

    public ConnectionManager() {

    }


    public ConcurrentHashMap<UUID, String> getPlayerTokens() {
        return playerTokens;
    }

    public ConcurrentHashMap<UUID, User> getUsers() {
        return users;
    }

    public ConcurrentHashMap<WebSocket, User> getSessionUsers() {
        return sessionUsers;
    }

    public void setRemotePeers(User playerTo, Collection<UUID> peers) {
        // Player to add
        for (UUID peer : peers)
            if (!playerTo.getRemotePeers().contains(peer)) linkPeers(playerTo, users.get(peer));

        // Player to remove
        for (UUID peer : playerTo.getRemotePeers())
            if (!peers.contains(peer)) unlinkPeers(playerTo, users.get(peer));
    }


    public void linkPeers(User player1, User player2) {
        UUID p1 = player1.getPlayerId();
        UUID p2 = player2.getPlayerId();
        Set<UUID> peers1 = player1.getRemotePeers();
        Set<UUID> peers2 = player2.getRemotePeers();
        if (peers1.contains(p2) || peers2.contains(p1)) {
            BoostedAudio.debug("Peers already set !");
            return;
        }
        peers1.add(p2);
        peers2.add(p1);

        AddPeerPacket peerPacket = new AddPeerPacket(new AddPeerPacket.RTCDescription("", "createoffer"), player1.getPlayerId(), player2.getPlayerId());

        /*String packet = Main.getGson().toJson(new PacketList(peerPacket));
        BoostedAudio.debug("Sending creating offer packet: " + packet);*/
        player2.sendPacket(peerPacket);
    }

    public void unlinkPeers(User player1, User player2) {
        player1.getRemotePeers().remove(player2.getPlayerId());
        player2.getRemotePeers().remove(player1.getPlayerId());
        player1.sendPacket(new PacketList(new RemovePeerPacket(player2.getPlayerId())));
        player2.sendPacket(new PacketList(new RemovePeerPacket(player1.getPlayerId())));
    }

}