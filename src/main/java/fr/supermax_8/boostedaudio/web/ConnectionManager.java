package fr.supermax_8.boostedaudio.web;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import fr.supermax_8.boostedaudio.web.packets.AddPeerPacket;
import org.eclipse.jetty.websocket.api.Session;

import java.util.*;

public class ConnectionManager {

    /**
     * UUID: Minecraft player uuid
     * String: token
     */
    protected final BiMap<UUID, String> playerTokens = HashBiMap.create();

    /**
     * The users trusted with the token link to the minecraft client
     * String: The token gave by the server in game
     * Session: The websocket session
     */
    protected final BiMap<UUID, User> users = HashBiMap.create();

    /**
     * UUID: PlayerID
     * List of User: The list of user that are connected to the player from the player id
     */
    protected final HashMap<UUID, List<User>> peerConnectionRegistry = new HashMap<>();

    protected final HashSet<Session> sessions = new HashSet<>();


    public ConnectionManager() {

    }


    public BiMap<UUID, String> getPlayerTokens() {
        return playerTokens;
    }

    public HashSet<Session> getSessions() {
        return sessions;
    }

    public BiMap<UUID, User> getUsers() {
        return users;
    }

    public void setRemotePeers(UUID playerTo, Collection<UUID> peers) {
        User user = users.get(playerTo);

        // Player to add
        for (UUID peer : peers)
            if (!user.getRemotePeers().contains(new User.Peer(peer))) linkPeers(user, users.get(peer));

        // Player to remove
        for (User.Peer peer : user.getRemotePeers()) {
            if (!peers.contains(peer.playerId())) unlinkPeers(user, users.get(peer.playerId()));
        }

    }


    public void linkPeers(User player1, User player2) {
        player1.getRemotePeers().add(new User.Peer(player2.getPlayerId()));
        player2.getRemotePeers().add(new User.Peer(player1.getPlayerId()));

        AddPeerPacket peerPacket = new AddPeerPacket(new AddPeerPacket.RTCDescription("", "createoffer"), player1.getPlayerId(), player2.getPlayerId());
        player1.send(peerPacket);
    }

    public void unlinkPeers(User player1, User player2) {

    }

}