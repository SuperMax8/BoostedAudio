package fr.supermax_8.boostedaudio.core.multiserv;

import java.util.UUID;

public interface ServerPacketListener {

    void onReceive(String message, UUID serverId);

}