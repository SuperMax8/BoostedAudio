package fr.supermax_8.boostedaudio.web.packets;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.web.Audio;
import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.Packet;
import fr.supermax_8.boostedaudio.web.User;

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

    public AddAudioPacket(UUID uuid, String link, int fadeIn, int fadeOut, Audio.AudioSpatialInfo spatialInfo) {
        this.uuid = uuid;
        this.link = link;
        this.spatialInfo = spatialInfo;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
    }

    @Override
    public void onReceive(User session, AudioWebSocketServer server) {
        session.getSession().close();
        BoostedAudio.debug("AddAudioPacket close() session");
    }


}