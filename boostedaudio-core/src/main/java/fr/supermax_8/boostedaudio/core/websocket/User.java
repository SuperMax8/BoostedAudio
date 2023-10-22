package fr.supermax_8.boostedaudio.core.websocket;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.Packet;
import fr.supermax_8.boostedaudio.core.websocket.packets.AddAudioPacket;
import fr.supermax_8.boostedaudio.core.websocket.packets.PausePlayAudioPacket;
import fr.supermax_8.boostedaudio.core.websocket.packets.RemoveAudioPacket;
import org.java_websocket.WebSocket;

import java.util.*;

public class User {

    private final Map<String, Set<UUID>> remotePeers = new HashMap<>();
    private final Map<UUID, Audio> playingAudio = new HashMap<>();
    private final WebSocket session;
    private final String connectionToken;
    private final UUID playerId;

    public User(WebSocket session, String connectionToken, UUID playerId) {
        this.session = session;
        this.connectionToken = connectionToken;
        this.playerId = playerId;
    }

    public Set<UUID> getRemotePeers(String layerId) {
        return remotePeers.computeIfAbsent(layerId, k -> new HashSet<>());
    }

    public Map<String, Set<UUID>> getRemotePeers() {
        return remotePeers;
    }

    public WebSocket getSession() {
        return session;
    }

    public String getConnectionToken() {
        return connectionToken;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Map<UUID, Audio> getPlayingAudio() {
        return playingAudio;
    }

    public Audio playAudio(String link, Audio.AudioSpatialInfo spatialInfo, int fade) {
        return playAudio(link, spatialInfo, fade, fade, false);
    }

    public Audio playAudio(String link, int fade) {
        return playAudio(link, null, fade, fade, false);
    }

    public Audio playAudio(String link, int fadeIn, int fadeOut) {
        return playAudio(link, null, fadeIn, fadeOut, false);
    }

    public Audio playAudio(String link, Audio.AudioSpatialInfo spatialInfo, int fadeIn, int fadeOut, boolean loop) {
        UUID id = UUID.randomUUID();
        Audio audio = new Audio(link, spatialInfo, id, fadeIn, fadeOut, loop);
        playAudio(audio);
        return audio;
    }

    public void playAudio(Audio audio) {
        AddAudioPacket packet = new AddAudioPacket(audio.getId(), audio.getLink(), audio.getFadeIn(), audio.getFadeOut(), audio.getSpatialInfo());
        playingAudio.put(audio.getId(), audio);
        sendPacket(packet);
    }

    public Audio pauseAudio(String link) {
        Audio audio = null;
        for (Audio audio1 : playingAudio.values()) if (audio1.getLink().equals(link)) audio = audio1;
        if (audio != null) pauseAudio(audio);
        return audio;
    }

    public Audio pauseAudio(UUID id) {
        Audio audio = playingAudio.get(id);
        pauseAudio(audio);
        return audio;
    }

    public void pauseAudio(Audio audio) {
        PausePlayAudioPacket packet = new PausePlayAudioPacket(audio.getId(), audio.getFadeOut());
        sendPacket(packet);
    }


    public Audio stopAudio(String link) {
        Audio audio = null;
        for (Audio audio1 : playingAudio.values()) if (audio1.getLink().equals(link)) audio = audio1;
        if (audio != null) stopAudio(audio);
        return audio;
    }

    public Audio stopAudio(UUID id) {
        Audio audio = playingAudio.get(id);
        stopAudio(audio);
        return audio;
    }

    public void stopAudio(Audio audio) {
        RemoveAudioPacket packet = new RemoveAudioPacket(audio.getId(), audio.getFadeOut());
        sendPacket(packet);
    }

    public void sendPacket(Packet... packets) {
        sendPacket(new PacketList(packets));
    }

    public void sendPacket(PacketList packetList) {
        String packet = BoostedAudioAPI.api.getGson().toJson(packetList);
        sendPacket(packet);
    }

    public void sendPacket(String packet) {
        if (session.isOpen()) session.send(packet);
    }

}