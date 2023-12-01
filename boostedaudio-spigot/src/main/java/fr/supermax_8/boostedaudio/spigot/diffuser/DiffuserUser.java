package fr.supermax_8.boostedaudio.spigot.diffuser;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.packet.Packet;
import fr.supermax_8.boostedaudio.api.user.User;
import fr.supermax_8.boostedaudio.api.user.Audio;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;
import fr.supermax_8.boostedaudio.api.packet.PacketList;
import fr.supermax_8.boostedaudio.core.websocket.packets.AddAudioPacket;
import fr.supermax_8.boostedaudio.core.websocket.packets.PausePlayAudioPacket;
import fr.supermax_8.boostedaudio.core.websocket.packets.RemoveAudioPacket;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DiffuserUser implements User {

    private final Map<String, Set<UUID>> remotePeers;

    private final Map<UUID, Audio> playingAudio;

    private final String connectionToken;

    private final UUID playerId;

    private final boolean muted;

    public DiffuserUser(HostUser user) {
        remotePeers = user.getRemotePeers();
        playingAudio = user.getPlayingAudio();
        connectionToken = user.getConnectionToken();
        playerId = user.getPlayerId();
        muted = user.isMuted();
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
        BoostedAudioSpigot.sendServerPacket("closeuser", playerId.toString());
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
        RemoveAudioPacket packet = new RemoveAudioPacket(audio.getId(), audio.getFadeOut());
        sendPacket(packet);
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
        if (BoostedAudioAPI.getAPI().getConfiguration().isDebugMode()) BoostedAudioAPI.getAPI().info("SendingPacket: " + packet);
        String message = playerId.toString() + ";" + packet;
        BoostedAudioSpigot.sendServerPacket("senduserpacket", message);
    }

    @Override
    public boolean isMuted() {
        return muted;
    }

    @Override
    public void setMuted(boolean muted, long endTime) {
        if (muted == this.muted) return;
        BoostedAudioSpigot.sendServerPacket("setmuted", playerId + ";" + muted + ";" + endTime);
    }

}