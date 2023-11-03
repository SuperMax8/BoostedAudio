package fr.supermax_8.boostedaudio.core.websocket.packets;

import fr.supermax_8.boostedaudio.api.Packet;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;

public class ServerChangePacket implements Packet {

    private final String serverName;

    public ServerChangePacket(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public void onReceive(HostUser session, AudioWebSocketServer server) {

    }

}