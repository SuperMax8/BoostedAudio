package fr.supermax_8.boostedaudio.core.websocket.packets;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.packet.Packet;
import fr.supermax_8.boostedaudio.api.audio.Audio;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;

import java.util.UUID;

public class AddAudioPacket implements Packet {

    private UUID uuid;
    private String link;
    private Audio.AudioSpatialInfo spatialInfo;

    /**
     * Fade in ms
     */
    private int fadeIn;

    /**
     * Fade in ms
     */
    private int fadeOut;

    private boolean synchronous;

    public AddAudioPacket(UUID uuid, String link, int fadeIn, int fadeOut, boolean synchronous, Audio.AudioSpatialInfo spatialInfo) {
        this.uuid = uuid;
        this.link = link;
        this.spatialInfo = spatialInfo;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
        this.synchronous = synchronous;
    }

    @Override
    public void onReceive(HostUser session, AudioWebSocketServer server) {
        session.getSession().close();
        BoostedAudioAPI.api.debug("AddAudioPacket close() session");
    }


}