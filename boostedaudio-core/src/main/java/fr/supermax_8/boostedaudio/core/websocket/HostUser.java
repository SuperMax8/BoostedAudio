package fr.supermax_8.boostedaudio.core.websocket;

import com.google.gson.annotations.Expose;
import fr.supermax_8.boostedaudio.api.user.Audio;
import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.packet.Packet;
import fr.supermax_8.boostedaudio.api.user.User;
import fr.supermax_8.boostedaudio.api.packet.PacketList;
import fr.supermax_8.boostedaudio.core.websocket.packets.AddAudioPacket;
import fr.supermax_8.boostedaudio.core.websocket.packets.PausePlayAudioPacket;
import fr.supermax_8.boostedaudio.core.websocket.packets.RemoveAudioPacket;
import org.java_websocket.WebSocket;

import java.util.*;

public class HostUser implements User {

    private final WebSocket session;

    @Expose
    private final Map<String, Set<UUID>> remotePeers = new HashMap<>();
    @Expose
    private final Map<UUID, Audio> playingAudio = new HashMap<>();
    @Expose
    private final String connectionToken;
    @Expose
    private final UUID playerId;

    private long waitUntil = 0;

    public HostUser(WebSocket session, String connectionToken, UUID playerId) {
        this.session = session;
        this.connectionToken = connectionToken;
        this.playerId = playerId;
    }

    public Set<UUID> getRemotePeers(String layerId) {
        return remotePeers.computeIfAbsent(layerId, k -> new HashSet<>());
    }

    @Override
    public Map<String, Set<UUID>> getRemotePeers() {
        return remotePeers;
    }

    public WebSocket getSession() {
        return session;
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
        session.close();
    }

    @Override
    public Audio playAudio(String link, Audio.AudioSpatialInfo spatialInfo, int fade) {
        return playAudio(link, spatialInfo, fade, fade, false);
    }

    @Override
    public Audio playAudio(String link, int fade) {
        return playAudio(link, null, fade, fade, false);
    }

    @Override
    public Audio playAudio(String link, int fadeIn, int fadeOut) {
        return playAudio(link, null, fadeIn, fadeOut, false);
    }

    @Override
    public Audio playAudio(String link, Audio.AudioSpatialInfo spatialInfo, int fadeIn, int fadeOut, boolean loop) {
        UUID id = UUID.randomUUID();
        Audio audio = new Audio(link, spatialInfo, id, fadeIn, fadeOut, loop);
        playAudio(audio);
        return audio;
    }

    @Override
    public void playAudio(Audio audio) {
        waitUntil();
        AddAudioPacket packet = new AddAudioPacket(audio.getId(), audio.getLink(), audio.getFadeIn(), audio.getFadeOut(), audio.getSpatialInfo());
        playingAudio.put(audio.getId(), audio);
        sendPacket(packet);
    }

    @Override
    public Audio pauseAudio(String link) {
        Audio audio = null;
        for (Audio audio1 : playingAudio.values()) if (audio1.getLink().equals(link)) audio = audio1;
        if (audio != null) pauseAudio(audio);
        return audio;
    }

    @Override
    public Audio pauseAudio(UUID id) {
        Audio audio = playingAudio.get(id);
        pauseAudio(audio);
        return audio;
    }

    @Override
    public void pauseAudio(Audio audio) {
        waitUntil();
        PausePlayAudioPacket packet = new PausePlayAudioPacket(audio.getId(), audio.getFadeOut());
        sendPacket(packet);
    }

    @Override
    public Audio stopAudio(String link) {
        Audio audio = null;
        for (Audio audio1 : playingAudio.values()) if (audio1.getLink().equals(link)) audio = audio1;
        if (audio != null) stopAudio(audio);
        return audio;
    }

    @Override
    public Audio stopAudio(UUID id) {
        Audio audio = playingAudio.get(id);
        stopAudio(audio);
        return audio;
    }

    @Override
    public void stopAudio(Audio audio) {
        waitUntil();
        RemoveAudioPacket packet = new RemoveAudioPacket(audio.getId(), audio.getFadeOut());
        sendPacket(packet);
        playingAudio.remove(audio.getId());
    }

    @Override
    public void sendPacket(Packet... packets) {
        sendPacket(new PacketList(packets));
    }

    @Override
    public void sendPacket(PacketList packetList) {
        String packet = BoostedAudioAPI.api.getGson().toJson(packetList);
        sendPacket(packet);
    }

    @Override
    public void sendPacket(String packet) {
        if (session.isOpen()) session.send(packet);
    }

    private void waitUntil() {
        while (waitUntil > System.currentTimeMillis()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void waitUntil(long time) {
        waitUntil = System.currentTimeMillis() + time;
    }

}