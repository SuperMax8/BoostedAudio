package fr.supermax_8.boostedaudio.web.packets;

import fr.supermax_8.boostedaudio.web.ClientWebSocket;
import fr.supermax_8.boostedaudio.web.Packet;
import fr.supermax_8.boostedaudio.web.User;

import java.util.UUID;

public class AddPeerPacket implements Packet {

    // This is answer or offer
    private final RTCDescription rtcDescription;

    private final UUID from;
    private final UUID to;

    public AddPeerPacket(RTCDescription rtcDescription, UUID from, UUID to) {
        this.rtcDescription = rtcDescription;
        this.from = from;
        this.to = to;
    }


    @Override
    public void onReceive(User user) {
        if (user.getRemotePeers().contains(new User.Peer(from)))
            ClientWebSocket.manager.getUsers().get(from).send(this);
        else user.getSession().close();
    }


    public record RTCDescription(String sdp, String type) {

    }

}