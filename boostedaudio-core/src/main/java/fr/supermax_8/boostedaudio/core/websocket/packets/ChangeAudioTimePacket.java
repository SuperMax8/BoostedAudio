package fr.supermax_8.boostedaudio.core.websocket.packets;

import fr.supermax_8.boostedaudio.api.packet.Packet;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;

import java.util.UUID;

public class ChangeAudioTimePacket implements Packet {

    private final float timeToPlay;
    private final UUID audioId;

    public ChangeAudioTimePacket(float timeToPlay, UUID audioId) {
        this.timeToPlay = timeToPlay;
        this.audioId = audioId;
    }


    @Override
    public void onReceive(HostUser session, AudioWebSocketServer server) {

    }


}