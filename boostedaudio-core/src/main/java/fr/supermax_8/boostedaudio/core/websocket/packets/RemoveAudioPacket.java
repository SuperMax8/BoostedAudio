package fr.supermax_8.boostedaudio.core.websocket.packets;

import fr.supermax_8.boostedaudio.core.websocket.Audio;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.api.Packet;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;

import java.util.Map;
import java.util.UUID;

public class RemoveAudioPacket implements Packet {

    private final UUID uuid;

    /**
     * Fade in ms
     */
    private final int fade;

    public RemoveAudioPacket(UUID uuid, int fade) {
        this.uuid = uuid;
        this.fade = fade;
    }

    @Override
    public void onReceive(HostUser session, AudioWebSocketServer server) {
        Map<UUID, Audio> audioMap = session.getPlayingAudio();
        Audio audio = audioMap.get(uuid);
        if (audio == null) {
            /*session.getSession().close();
            BoostedAudio.debug("RemoveAudioPacket close() session");*/
            return;
        }
        if (audio.isLoop()) {
            AddAudioPacket packet = new AddAudioPacket(audio.getId(), audio.getLink(), audio.getFadeIn(), audio.getFadeOut(), audio.getSpatialInfo());
            session.sendPacket(packet);
        } else audioMap.remove(audio.getId());
    }

}