package fr.supermax_8.boostedaudio.core.websocket.packets;

import fr.supermax_8.boostedaudio.api.packet.Packet;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;

public class ServerMutePacket implements Packet {

    private final boolean mute;

    public ServerMutePacket(boolean mute) {
        this.mute = mute;
    }

    @Override
    public void onReceive(HostUser session, AudioWebSocketServer server) {
        session.close();
    }


}