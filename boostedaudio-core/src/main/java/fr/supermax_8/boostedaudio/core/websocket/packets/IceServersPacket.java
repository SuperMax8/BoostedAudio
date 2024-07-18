package fr.supermax_8.boostedaudio.core.websocket.packets;

import com.google.gson.JsonArray;
import fr.supermax_8.boostedaudio.api.packet.Packet;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;

public class IceServersPacket implements Packet {

    private final JsonArray iceServers;

    public IceServersPacket(JsonArray iceServers) {
        this.iceServers = iceServers;
    }

    @Override
    public void onReceive(HostUser session, AudioWebSocketServer server) {
        session.close();
    }


}