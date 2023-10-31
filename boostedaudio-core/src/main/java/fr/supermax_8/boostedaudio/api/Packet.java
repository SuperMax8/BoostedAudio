package fr.supermax_8.boostedaudio.api;

import fr.supermax_8.boostedaudio.core.websocket.HostUser;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;

public interface Packet {

    void onReceive(HostUser session, AudioWebSocketServer server);

}