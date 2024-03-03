package fr.supermax_8.boostedaudio.core.websocket.packets;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.packet.Packet;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;

import java.util.UUID;

public class AddPeerPacket implements Packet {

    // This is answer or offer
    private final RTCDescription rtcDesc;

    private final UUID from;
    private final UUID to;
    private String username;
    private final String layerId;
    private final boolean spatialized;

    public AddPeerPacket(String layerId, RTCDescription rtcDesc, UUID from, UUID to, String username, boolean spatialized) {
        this.layerId = layerId;
        this.rtcDesc = rtcDesc;
        this.from = from;
        this.to = to;
        this.username = username;
        this.spatialized = spatialized;
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
    public void onReceive(HostUser user, AudioWebSocketServer server) {
/*        BoostedAudio.debug("RECEIVED PEER MESSAGE FROM " + user.getPlayerId());
        BoostedAudio.debug("TO " + to);
        BoostedAudio.debug("FROM " + from);
        BoostedAudio.debug("PEERS" + user.getRemotePeers());*/
        if (user.getRemotePeers(layerId).contains(to)) {
            if (rtcDesc.type.equals("offer")) username = BoostedAudioAPI.getAPI().getInternalAPI().getUsername(from);
            server.manager.getUsers().get(to).sendPacket(this);
        }
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