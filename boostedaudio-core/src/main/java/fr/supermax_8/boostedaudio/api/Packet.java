package fr.supermax_8.boostedaudio.api;

import fr.supermax_8.boostedaudio.core.websocket.User;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;

public interface Packet {

    void onReceive(User session, AudioWebSocketServer server);

}