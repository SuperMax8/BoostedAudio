package fr.supermax_8.boostedaudio.web.packets;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.web.AudioWebSocket;
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
    public void onReceive(User session, AudioWebSocket server) {
        try {
            session.getSession().close();
        } catch (Exception ex) {
        }
        BoostedAudio.debug("RemovePeerPacket close() session");
    }

}