package fr.supermax_8.boostedaudio.websocket.packets;

import fr.supermax_8.boostedaudio.WebRTCSocket;
import fr.supermax_8.boostedaudio.websocket.Packet;
import org.eclipse.jetty.websocket.api.Session;

public class RTCSessionDescriptionPacket implements Packet {

    private String json;


    @Override
    public void onReceive(Session session, WebRTCSocket socket) {

    }

}