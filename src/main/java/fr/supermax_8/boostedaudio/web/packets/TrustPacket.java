package fr.supermax_8.boostedaudio.web.packets;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.Packet;
import fr.supermax_8.boostedaudio.web.User;

import java.util.Map;
import java.util.UUID;

public class TrustPacket implements Packet {

    private final String token;

    public TrustPacket(String token) {
        this.token = token;
    }

    @Override
    public void onReceive(User user, AudioWebSocketServer server) {
        UUID playerId;

        if ((playerId = getKeyByValue(server.manager.getPlayerTokens(), token)) != null) {
            User newUser = new User(user.getSession(), token, playerId);
            server.manager.getUsers().put(playerId, newUser);
            server.manager.getSessionUsers().put(user.getSession(), newUser);
            BoostedAudio.debug("New trusted: " + playerId);
        } else user.getSession().close();
    }


    public static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) if (entry.getValue().equals(value)) return entry.getKey();
        return null;
    }

}