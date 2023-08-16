package fr.supermax_8.boostedaudio.web;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import fr.supermax_8.boostedaudio.web.packets.AddPeerPacket;
import fr.supermax_8.boostedaudio.web.packets.RemovePeerPacket;
import org.java_websocket.WebSocket;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConnectionManager {

    /**
     * UUID: Minecraft player uuid
     * String: token
     */
    protected final BiMap<UUID, String> playerTokens = HashBiMap.create(Map.of(
            UUID.fromString("9008ec29-3e84-4afa-918a-f6f9f923c8b8"), "zougoulou",
            UUID.fromString("7b1c5ff6-aec7-4a8c-9864-d35838c653bb"), "zougouloux"
    ));

    /**
     * The users trusted with the token link to the minecraft client
     * String: The token gave by the server in game
     * Session: The websocket session
     */
    protected final HashMap<UUID, User> users = new HashMap<>();

    protected final HashMap<WebSocket, User> sessionUsers = new HashMap<>();

    public ConnectionManager() {

    }


    public BiMap<UUID, String> getPlayerTokens() {
        return playerTokens;
    }

    public HashMap<UUID, User> getUsers() {
        return users;
    }

    public HashMap<WebSocket, User> getSessionUsers() {
        return sessionUsers;
    }

    public void setRemotePeers(UUID playerTo, Collection<UUID> peers) {
        User user = users.get(playerTo);

        // Player to add
        for (UUID peer : peers)
            if (!user.getRemotePeers().contains(peer)) linkPeers(user, users.get(peer));

        // Player to remove
        for (UUID peer : user.getRemotePeers())
            if (!peers.contains(peer)) unlinkPeers(user, users.get(peer));
    }


    public void linkPeers(User player1, User player2) {
        player1.getRemotePeers().add(player2.getPlayerId());
        player2.getRemotePeers().add(player1.getPlayerId());

        AddPeerPacket peerPacket = new AddPeerPacket(new AddPeerPacket.RTCDescription("", "createoffer"), player1.getPlayerId(), player2.getPlayerId());

        /*String packet = Main.getGson().toJson(new PacketList(peerPacket));
        System.out.println("Sending creating offer packet: " + packet);*/
        player2.send(peerPacket);
    }

    public void unlinkPeers(User player1, User player2) {
        player1.send(new PacketList(new RemovePeerPacket(player2.getPlayerId())));
        player2.send(new PacketList(new RemovePeerPacket(player1.getPlayerId())));
    }

}