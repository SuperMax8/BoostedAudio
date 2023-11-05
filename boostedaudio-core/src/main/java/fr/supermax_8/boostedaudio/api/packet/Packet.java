package fr.supermax_8.boostedaudio.api.packet;

import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;

public interface Packet {

    void onReceive(HostUser session, AudioWebSocketServer server);


}