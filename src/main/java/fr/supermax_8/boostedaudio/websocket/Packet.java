package fr.supermax_8.boostedaudio.websocket;

import fr.supermax_8.boostedaudio.WebRTCSocket;
import org.eclipse.jetty.websocket.api.Session;

public interface Packet {

    void onReceive(Session session, WebRTCSocket socket);

}