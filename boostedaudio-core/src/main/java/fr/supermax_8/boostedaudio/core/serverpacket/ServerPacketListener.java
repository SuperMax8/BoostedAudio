package fr.supermax_8.boostedaudio.core.serverpacket;

import java.util.UUID;

public interface ServerPacketListener {

    void onResponse(String message, UUID serverId);

}