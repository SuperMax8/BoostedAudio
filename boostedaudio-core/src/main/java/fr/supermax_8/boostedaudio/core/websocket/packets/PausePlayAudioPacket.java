package fr.supermax_8.boostedaudio.core.websocket.packets;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.Packet;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;

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
    public void onReceive(HostUser session, AudioWebSocketServer server) {
        session.getSession().close();
        BoostedAudioAPI.api.debug("RemovePeerPacket close() session");
    }

}