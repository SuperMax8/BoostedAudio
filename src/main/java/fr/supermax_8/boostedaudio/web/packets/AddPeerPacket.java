package fr.supermax_8.boostedaudio.web.packets;

import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.Packet;
import fr.supermax_8.boostedaudio.web.User;

import java.util.UUID;

public class AddPeerPacket implements Packet {

    // This is answer or offer
    private final RTCDescription rtcDesc;

    private final UUID from;
    private final UUID to;

    public AddPeerPacket(RTCDescription rtcDesc, UUID from, UUID to) {
        this.rtcDesc = rtcDesc;
        this.from = from;
        this.to = to;
    }


    @Override
    public void onReceive(User user, AudioWebSocketServer server) {
        System.out.println("RECEIVED PEER MESSAGE FROM " + user.getPlayerId());
        System.out.println("TO " + to);
        System.out.println("FROM " + from);
        System.out.println("PEERS" + user.getRemotePeers());
        if (user.getRemotePeers().contains(to))
            server.manager.getUsers().get(to).send(this);
        else {
            user.getSession().close();
            System.out.println("KickPEER");
        }
    }


    public record RTCDescription(String sdp, String type) {

    }

}