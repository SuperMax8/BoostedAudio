package fr.supermax_8.boostedaudio.web.packets;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.web.AudioWebSocket;
import fr.supermax_8.boostedaudio.web.Packet;
import fr.supermax_8.boostedaudio.web.User;

import java.util.UUID;

public class RemovePeerPacket implements Packet {

    private final UUID playerToRemove;

    public RemovePeerPacket(UUID playerToRemove) {
        this.playerToRemove = playerToRemove;
    }

    @Override
    public void onReceive(User session, AudioWebSocket server) {
        try {
            session.getSession().close();
        } catch (Exception ex) {
        }
        BoostedAudio.debug("RemovePeerPacket close() session");
    }

}