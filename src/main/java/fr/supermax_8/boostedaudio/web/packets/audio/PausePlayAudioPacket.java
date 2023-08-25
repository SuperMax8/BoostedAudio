package fr.supermax_8.boostedaudio.web.packets.audio;

import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.Packet;
import fr.supermax_8.boostedaudio.web.User;

import java.util.UUID;

public class PausePlayAudioPacket implements Packet {

    private UUID uuid;

    /**
     * Fade in ms
     */
    private int fade;

    public PausePlayAudioPacket(UUID uuid, int fade) {
        this.uuid = uuid;
        this.fade = fade;
    }

    @Override
    public void onReceive(User session, AudioWebSocketServer server) {
        session.getSession().close();
    }

}