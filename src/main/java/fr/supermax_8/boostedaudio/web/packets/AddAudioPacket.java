package fr.supermax_8.boostedaudio.web.packets;

import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.Packet;
import fr.supermax_8.boostedaudio.web.User;

public class AddAudioPacket implements Packet {

    

    @Override
    public void onReceive(User session, AudioWebSocketServer server) {
        session.getSession().close();
    }

}