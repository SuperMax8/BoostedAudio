package fr.supermax_8.boostedaudio.web.packets;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.utils.SerializableLocation;
import fr.supermax_8.boostedaudio.web.AudioWebSocket;
import fr.supermax_8.boostedaudio.web.Packet;
import fr.supermax_8.boostedaudio.web.User;

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
    public void onReceive(User session, AudioWebSocket server) {
        try {
            session.getSession().close();
        } catch (Exception e) {}
        BoostedAudio.debug("UpdateVocalPositionsPacket close() session");
    }

}