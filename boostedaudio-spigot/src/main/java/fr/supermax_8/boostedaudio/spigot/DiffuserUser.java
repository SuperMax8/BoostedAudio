package fr.supermax_8.boostedaudio.spigot;

import fr.supermax_8.boostedaudio.api.Packet;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.core.websocket.Audio;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;
import fr.supermax_8.boostedaudio.core.websocket.PacketList;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DiffuserUser implements User {

    private final Map<String, Set<UUID>> remotePeers;

    private final Map<UUID, Audio> playingAudio;

    private final String connectionToken;

    private final UUID playerId;

    public DiffuserUser(HostUser user) {
        remotePeers = user.getRemotePeers();
        playingAudio = user.getPlayingAudio();
        connectionToken = user.getConnectionToken();
        playerId = user.getPlayerId();
    }

    @Override
    public Map<String, Set<UUID>> getRemotePeers() {
        return remotePeers;
    }

    @Override
    public String getConnectionToken() {
        return connectionToken;
    }

    @Override
    public UUID getPlayerId() {
        return playerId;
    }

    @Override
    public Map<UUID, Audio> getPlayingAudio() {
        return playingAudio;
    }

    @Override
    public void close() {

    }

    @Override
    public Audio playAudio(String link, Audio.AudioSpatialInfo spatialInfo, int fade) {
        return null;
    }

    @Override
    public Audio playAudio(String link, int fade) {
        return null;
    }

    @Override
    public Audio playAudio(String link, int fadeIn, int fadeOut) {
        return null;
    }

    @Override
    public Audio playAudio(String link, Audio.AudioSpatialInfo spatialInfo, int fadeIn, int fadeOut, boolean loop) {
        return null;
    }

    @Override
    public void playAudio(Audio audio) {

    }

    @Override
    public Audio pauseAudio(String link) {
        return null;
    }

    @Override
    public Audio pauseAudio(UUID id) {
        return null;
    }

    @Override
    public void pauseAudio(Audio audio) {

    }

    @Override
    public Audio stopAudio(String link) {
        return null;
    }

    @Override
    public Audio stopAudio(UUID id) {
        return null;
    }

    @Override
    public void stopAudio(Audio audio) {

    }

    @Override
    public void sendPacket(Packet... packets) {

    }

    @Override
    public void sendPacket(PacketList packetList) {

    }

    @Override
    public void sendPacket(String packet) {

    }

}