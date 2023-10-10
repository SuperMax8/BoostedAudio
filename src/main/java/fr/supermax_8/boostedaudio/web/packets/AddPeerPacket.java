package fr.supermax_8.boostedaudio.web.packets;

import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.Packet;
import fr.supermax_8.boostedaudio.web.User;
import org.bukkit.Bukkit;

import java.util.UUID;

public class AddPeerPacket implements Packet {

    // This is answer or offer
    private final RTCDescription rtcDesc;

    private final UUID from;
    private final UUID to;
    private String username;

    public AddPeerPacket(RTCDescription rtcDesc, UUID from, UUID to, String username) {
        this.rtcDesc = rtcDesc;
        this.from = from;
        this.to = to;
        this.username = username;
    }

    /**
     * Utilization Scheme
     * Server ->1 ADP createOffer (createPC)
     * Client ->server ADP offer
     * Server ->2 ADP offer (createPC)
     * Client -> ADP answer
     * Server ->1 ADP answer
     */
    @Override
    public void onReceive(User user, AudioWebSocketServer server) {
/*        BoostedAudio.debug("RECEIVED PEER MESSAGE FROM " + user.getPlayerId());
        BoostedAudio.debug("TO " + to);
        BoostedAudio.debug("FROM " + from);
        BoostedAudio.debug("PEERS" + user.getRemotePeers());*/
        try {
            username = Bukkit.getPlayer(from).getName();
        } catch (Exception e) {
        }
        if (user.getRemotePeers().contains(to))
            server.manager.getUsers().get(to).sendPacket(this);
    }

    public static class RTCDescription {

        private final String sdp;
        private final String type;

        public RTCDescription(String sdp, String type) {
            this.sdp = sdp;
            this.type = type;
        }

        public String getSdp() {
            return sdp;
        }

        public String getType() {
            return type;
        }
        
    }

}