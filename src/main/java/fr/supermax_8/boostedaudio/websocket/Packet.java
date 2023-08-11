package fr.supermax_8.boostedaudio.websocket;

import org.eclipse.jetty.websocket.api.Session;

public interface Packet {

    void onReceive(Session session, ClientWebSocket socket);

}