package fr.supermax_8.boostedaudio.core.websocket.packets;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.packet.Packet;
import fr.supermax_8.boostedaudio.api.audio.Audio;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;

import java.util.UUID;

public class AddAudioPacket implements Packet {

    private final UUID uuid;
    private final Audio.AudioPlayInfo playInfo;
    private final Audio.AudioSpatialInfo spatialInfo;


    public AddAudioPacket(UUID uuid, Audio.AudioPlayInfo playInfo, Audio.AudioSpatialInfo spatialInfo) {
        this.uuid = uuid;
        this.playInfo = playInfo;
        this.spatialInfo = spatialInfo;
    }

    @Override
    public void onReceive(HostUser session, AudioWebSocketServer server) {
        session.getSession().close();
        BoostedAudioAPI.api.debug("AddAudioPacket close() session");
    }


}