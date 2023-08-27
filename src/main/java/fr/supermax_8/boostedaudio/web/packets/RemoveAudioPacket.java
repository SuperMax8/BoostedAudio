package fr.supermax_8.boostedaudio.web.packets;

import fr.supermax_8.boostedaudio.web.Audio;
import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.Packet;
import fr.supermax_8.boostedaudio.web.User;

import java.util.Map;
import java.util.UUID;

public class RemoveAudioPacket implements Packet {

    private UUID uuid;

    /**
     * Fade in ms
     */
    private int fade;

    public RemoveAudioPacket(UUID uuid, int fade) {
        this.uuid = uuid;
        this.fade = fade;
    }

    @Override
    public void onReceive(User session, AudioWebSocketServer server) {
        Map<UUID, Audio> audioMap = session.getPlayingAudio();
        Audio audio = audioMap.get(uuid);
        if (audio == null) {
            session.getSession().close();
            return;
        }
        if (audio.isLoop()) {
            AddAudioPacket packet = new AddAudioPacket(audio.getId(), audio.getLink(), 0, 0, audio.getSpatialInfo());
            session.sendPacket(packet);
        } else audioMap.remove(audio.getId());
    }

}