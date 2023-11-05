package fr.supermax_8.boostedaudio.core.websocket.packets;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.packet.Packet;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;

import java.util.UUID;

public class RemovePeerPacket implements Packet {

    private final String layerId;
    private final UUID playerToRemove;

    public RemovePeerPacket(String layerId, UUID playerToRemove) {
        this.layerId = layerId;
        this.playerToRemove = playerToRemove;
    }

    @Override
    public void onReceive(HostUser session, AudioWebSocketServer server) {
        session.getSession().close();
        BoostedAudioAPI.api.debug("RemovePeerPacket close() session");
    }

}