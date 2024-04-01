package fr.supermax_8.boostedaudio.core.websocket.packets;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.packet.Packet;
import fr.supermax_8.boostedaudio.api.audio.Audio;
import fr.supermax_8.boostedaudio.core.utils.SerializableLocation;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;

import java.util.UUID;

public class UpdateAudioLocationPacket implements Packet {

    private final UUID audioId;
    private final SerializableLocation newLocation;

    public UpdateAudioLocationPacket(Audio audio, SerializableLocation newLocation) {
        this(audio.getId(), newLocation);
    }

    public UpdateAudioLocationPacket(UUID audioId, SerializableLocation newLocation) {
        this.audioId = audioId;
        this.newLocation = newLocation;
    }

    @Override
    public void onReceive(HostUser session, AudioWebSocketServer server) {
        session.getSession().close();
        BoostedAudioAPI.api.debug("UpdateAudioLocationPacket close() session");
    }


}