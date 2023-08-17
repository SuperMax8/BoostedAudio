package fr.supermax_8.boostedaudio.web;

import fr.supermax_8.boostedaudio.utils.HashBiMap;
import fr.supermax_8.boostedaudio.web.packets.AddPeerPacket;
import fr.supermax_8.boostedaudio.web.packets.RemovePeerPacket;
import org.java_websocket.WebSocket;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class ConnectionManager {

    /**
     * UUID: Minecraft player uuid
     * String: token
     */
    protected final HashBiMap<UUID, String> playerTokens = new HashBiMap<>(new HashMap<UUID, String>() {{
        put(UUID.fromString("d2491bf7-e8bb-4eba-8721-bbec75d3af0c"), "SuperMax_8");
        put(UUID.fromString("cce44059-edce-4801-89ba-acac1acfd459"), "Ender_Griefeur99");
    }});

    /**
     * The users trusted with the token link to the minecraft client
     * String: The token gave by the server in game
     * Session: The websocket session
     */
    protected final HashMap<UUID, User> users = new HashMap<>();

    protected final HashMap<WebSocket, User> sessionUsers = new HashMap<>();

    public ConnectionManager() {

    }


    public HashBiMap<UUID, String> getPlayerTokens() {
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
        BoostedAudio.debug("Sending creating offer packet: " + packet);*/
        player2.send(peerPacket);
    }

    public void unlinkPeers(User player1, User player2) {
        player1.getRemotePeers().remove(player2.getPlayerId());
        player2.getRemotePeers().remove(player1.getPlayerId());
        player1.send(new PacketList(new RemovePeerPacket(player2.getPlayerId())));
        player2.send(new PacketList(new RemovePeerPacket(player1.getPlayerId())));
    }

}