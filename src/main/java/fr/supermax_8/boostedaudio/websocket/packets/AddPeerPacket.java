package fr.supermax_8.boostedaudio.websocket.packets;

import fr.supermax_8.boostedaudio.websocket.ClientWebSocket;
import fr.supermax_8.boostedaudio.websocket.Packet;
import org.eclipse.jetty.websocket.api.Session;

public class AddPeerPacket implements Packet {

    // This is answer or offer
    private RTCDescription rtcDescription;

    @Override
    public void onReceive(Session session, ClientWebSocket socket) {
        socket.sendPackets(session, this);
    }


    public record RTCDescription(String sdp, String type) {
    }

}