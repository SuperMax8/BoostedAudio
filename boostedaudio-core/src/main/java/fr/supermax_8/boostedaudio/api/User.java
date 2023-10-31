package fr.supermax_8.boostedaudio.api;

import fr.supermax_8.boostedaudio.core.websocket.Audio;
import fr.supermax_8.boostedaudio.core.websocket.PacketList;

import java.util.UUID;

public interface User {

    UUID getPlayerId();

    void close();

    Audio playAudio(String link, Audio.AudioSpatialInfo spatialInfo, int fade);

    Audio playAudio(String link, int fade);

    Audio playAudio(String link, int fadeIn, int fadeOut);

    Audio playAudio(String link, Audio.AudioSpatialInfo spatialInfo, int fadeIn, int fadeOut, boolean loop);

    void playAudio(Audio audio);

    Audio pauseAudio(String link);

    Audio pauseAudio(UUID id);

    void pauseAudio(Audio audio);

    Audio stopAudio(String link);

    Audio stopAudio(UUID id);

    void stopAudio(Audio audio);

    void sendPacket(Packet... packets);

    void sendPacket(PacketList packetList);

}