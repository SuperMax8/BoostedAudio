package fr.supermax_8.boostedaudio.core.websocket.packets;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.packet.Packet;
import fr.supermax_8.boostedaudio.core.utils.SerializableLocation;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;

import java.util.Map;
import java.util.UUID;

public class UpdateVocalPositionsPacket implements Packet {

    private final SerializableLocation clientLoc;
    private final Map<UUID, SerializableLocation> playersAround;

    public UpdateVocalPositionsPacket(SerializableLocation clientLoc, Map<UUID, SerializableLocation> playersAround) {
        this.clientLoc = clientLoc;
        this.playersAround = playersAround;
    }

    @Override
    public void onReceive(HostUser session, AudioWebSocketServer server) {
        session.getSession().close();
        BoostedAudioAPI.api.debug("UpdateVocalPositionsPacket close() session");
    }

}