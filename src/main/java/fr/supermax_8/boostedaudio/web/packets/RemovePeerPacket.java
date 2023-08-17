package fr.supermax_8.boostedaudio.web.packets;

import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.Packet;
import fr.supermax_8.boostedaudio.web.User;

import java.util.UUID;

public class RemovePeerPacket implements Packet {

    private final UUID playerToRemove;

    public RemovePeerPacket(UUID playerToRemove) {
        this.playerToRemove = playerToRemove;
    }

    @Override
    public void onReceive(User session, AudioWebSocketServer server) {
        session.getSession().close();
    }

}