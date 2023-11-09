package fr.supermax_8.boostedaudio.core.websocket.packets;

import fr.supermax_8.boostedaudio.api.packet.Packet;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;

public class MutePacket implements Packet {

    private final boolean mute;

    public MutePacket(boolean mute) {
        this.mute = mute;
    }

    @Override
    public void onReceive(HostUser session, AudioWebSocketServer server) {
        session.close();
    }


}