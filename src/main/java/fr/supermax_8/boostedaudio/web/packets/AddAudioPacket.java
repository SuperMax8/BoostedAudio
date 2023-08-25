package fr.supermax_8.boostedaudio.web.packets;

import fr.supermax_8.boostedaudio.utils.SerializableLocation;
import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.Packet;
import fr.supermax_8.boostedaudio.web.User;

import java.util.UUID;

public class AddAudioPacket implements Packet {

    private UUID uuid;
    private String link;
    private SerializableLocation location;

    /**
     * Fade in ms
     */
    private int fade;

    public AddAudioPacket(UUID uuid, String link, int fade, SerializableLocation location) {
        this.uuid = uuid;
        this.link = link;
        this.location = location;
        this.fade = fade;
    }

    @Override
    public void onReceive(User session, AudioWebSocketServer server) {
        session.getSession().close();
    }


}