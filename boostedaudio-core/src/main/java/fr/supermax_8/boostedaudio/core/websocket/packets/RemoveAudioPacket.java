package fr.supermax_8.boostedaudio.core.websocket.packets;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.packet.Packet;
import fr.supermax_8.boostedaudio.api.audio.Audio;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;

import java.util.Map;
import java.util.UUID;

public class RemoveAudioPacket implements Packet {

    private final UUID uuid;

    /**
     * Fade in ms
     */
    private final int fade;
    private String audioLink = null;

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
        BoostedAudioAPI.getAPI().debug("RemoveAudio received: " + audio.getId());
        if (audio.isLoop() || audio.isSynchronous() || audio.getPlayList().isSynchronous()) {
            AddAudioPacket packet = new AddAudioPacket(audio.getId(), audio.getPlayInfo(audioLink), audio.getSpatialInfo());
            session.sendPacket(packet);
        } else audioMap.remove(audio.getId());
    }

}