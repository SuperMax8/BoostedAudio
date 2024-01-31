package fr.supermax_8.boostedaudio.api.user;

import fr.supermax_8.boostedaudio.api.packet.Packet;
import fr.supermax_8.boostedaudio.api.packet.PacketList;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface User {

    Map<String, Set<UUID>> getRemotePeers();

    String getConnectionToken();

    UUID getPlayerId();

    Map<UUID, Audio> getPlayingAudio();

    void close();

    Audio playAudio(String link, Audio.AudioSpatialInfo spatialInfo, int fade);

    Audio playAudio(String link, int fade);

    Audio playAudio(String link, int fadeIn, int fadeOut);

    Audio playAudio(String link, Audio.AudioSpatialInfo spatialInfo, int fadeIn, int fadeOut, boolean loop, boolean syncronous);

    void playAudio(Audio audio);

    Audio pauseAudio(String link);

    Audio pauseAudio(UUID id);

    void pauseAudio(Audio audio);

    Audio stopAudio(String link);

    Audio stopAudio(UUID id);

    void stopAudio(Audio audio);

    void sendPacket(Packet... packets);

    void sendPacket(PacketList packetList);

    void sendPacket(String packet);

    boolean isMuted();
    boolean isClientMuted();

    void setMuted(boolean muted, long endTime);

}