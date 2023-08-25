package fr.supermax_8.boostedaudio.web;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.utils.SerializableLocation;
import fr.supermax_8.boostedaudio.web.packets.AddAudioPacket;
import fr.supermax_8.boostedaudio.web.packets.PausePlayAudioPacket;
import fr.supermax_8.boostedaudio.web.packets.RemoveAudioPacket;
import org.java_websocket.WebSocket;

import java.util.*;

public class User {

    private final Set<UUID> remotePeers = new HashSet<>();
    private final Map<UUID, Audio> playingAudio = new HashMap<>();
    private final WebSocket session;
    private final String connectionToken;
    private final UUID playerId;

    public User(WebSocket session, String connectionToken, UUID playerId) {
        this.session = session;
        this.connectionToken = connectionToken;
        this.playerId = playerId;
    }

    public Set<UUID> getRemotePeers() {
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

    public Audio playAudio(String link, SerializableLocation location, int fade) {
        return playAudio(link, location, fade, fade, false);
    }

    public Audio playAudio(String link, int fade) {
        return playAudio(link, null, fade, fade, false);
    }

    public Audio playAudio(String link, int fadeIn, int fadeOut) {
        return playAudio(link, null, fadeIn, fadeOut, false);
    }

    public Audio playAudio(String link, SerializableLocation location, int fadeIn, int fadeOut, boolean loop) {
        UUID id = UUID.randomUUID();
        Audio audio = new Audio(link, location, id, fadeIn, fadeOut, loop);
        AddAudioPacket packet = new AddAudioPacket(id, link, fadeIn, location);
        playingAudio.put(id, audio);
        sendPacket(packet);
        return audio;
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
        String packet = BoostedAudio.getGson().toJson(packetList);
        sendPacket(packet);
    }

    public void sendPacket(String packet) {
        if (session.isOpen()) session.send(packet);
    }

}