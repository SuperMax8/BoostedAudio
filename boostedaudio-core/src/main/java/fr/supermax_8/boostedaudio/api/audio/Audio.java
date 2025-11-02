package fr.supermax_8.boostedaudio.api.audio;

import com.google.gson.annotations.Expose;
import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.User;
import fr.supermax_8.boostedaudio.api.packet.Packet;
import fr.supermax_8.boostedaudio.core.utils.FFmpegUtils;
import fr.supermax_8.boostedaudio.core.utils.HttpUtils;
import fr.supermax_8.boostedaudio.core.utils.SerializableLocation;
import fr.supermax_8.boostedaudio.core.websocket.packets.ChangeAudioTimePacket;
import fr.supermax_8.boostedaudio.core.websocket.packets.UpdateAudioLocationPacket;
import lombok.Getter;
import lombok.Setter;
import org.wildfly.common.annotation.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Audio {

    @Expose
    private final PlayList playList;
    @Expose
    private final HashSet<UUID> currentListeners = new HashSet<>();
    @Nullable
    @Expose
    private final AudioSpatialInfo spatialInfo;
    @Expose
    private final UUID id;
    @Expose
    private final int fadeIn;
    @Expose
    private final int fadeOut;
    @Expose
    private final boolean loop;
    @Expose
    private final boolean synchronous;
    @Expose
    private String currentPlayingLink = null;

    public Audio(String link, AudioSpatialInfo spatialInfo, UUID id, int fadeIn, int fadeOut, boolean loop, boolean synchronous) {
        this(Collections.singletonList(link), spatialInfo, id, fadeIn, fadeOut, loop, synchronous);
    }

    public Audio(List<String> links, AudioSpatialInfo spatialInfo, UUID id, int fadeIn, int fadeOut, boolean loop, boolean synchronous) {
        this(new PlayList(links, false), spatialInfo, id, fadeIn, fadeOut, loop, synchronous);
    }

    public Audio(PlayList playList, AudioSpatialInfo spatialInfo, UUID id, int fadeIn, int fadeOut, boolean loop, boolean synchronous) {
        this.playList = playList;
        this.spatialInfo = spatialInfo;
        this.id = id;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
        this.loop = loop;
        this.synchronous = synchronous;
    }

    public AudioPlayInfo getPlayInfo() {
        AudioPlayInfo audioPlayInfo = new AudioPlayInfo();
        if (synchronous || playList.isSynchronous())
            syncPlayInfo(audioPlayInfo);
        else
            audioPlayInfo.setLink(getRandomString(playList.getLinks()));
        currentPlayingLink = audioPlayInfo.link;
        setDefaultData(audioPlayInfo);
        return audioPlayInfo;
    }

    public AudioPlayInfo getPlayInfo(String oldLink) {
        AudioPlayInfo audioPlayInfo = new AudioPlayInfo();
        if (synchronous || playList.isSynchronous()) syncPlayInfo(audioPlayInfo);
        else {
            List<String> l = new ArrayList<>(playList.getLinks());
            if (l.size() > 1) l.remove(oldLink);
            audioPlayInfo.setLink(getRandomString(l));
        }
        currentPlayingLink = audioPlayInfo.link;
        setDefaultData(audioPlayInfo);
        return audioPlayInfo;
    }

    private void setDefaultData(AudioPlayInfo audioPlayInfo) {
        audioPlayInfo.setFadeIn(fadeIn);
        audioPlayInfo.setFadeOut(fadeOut);
    }

    private void syncPlayInfo(AudioPlayInfo playInfo) {
        List<String> soundUrls = playList.getLinks();
        double currentTimestampSeconds = System.currentTimeMillis() / 1000.0;
        // Calculate the total duration of all sounds
        double totalDuration = 0.0;
        List<Double> durations = new ArrayList<>();
        for (String url : soundUrls) {
            if (!url.startsWith("http"))
                url = HttpUtils.combineUrl(BoostedAudioAPI.getAPI().getConfiguration().getClientLink(), url);
            double duration = FFmpegUtils.getAudioDuration(url);
            BoostedAudioAPI.getAPI().debug("Duration for " + url + "  : " + duration);
            durations.add(duration);
            totalDuration += duration;
        }

        // Find the position in the timeline based on the current timestamp in seconds
        double positionInLoop = currentTimestampSeconds % totalDuration;

        // Identify which sound corresponds to this position
        double accumulatedDuration = 0.0;
        BoostedAudioAPI.getAPI().debug("positionInLoop " + positionInLoop);
        for (int i = 0; i < soundUrls.size(); i++) {
            double next = accumulatedDuration + durations.get(i);
            if (positionInLoop < next) {
                playInfo.setLink(soundUrls.get(i));
                playInfo.setTimeToPlay(positionInLoop - accumulatedDuration);
                return;
            }
            accumulatedDuration = next;
        }

        // Fallback (should not happen if logic is correct)
        BoostedAudioAPI.getAPI().info("§cError while getting link synchronous on playlist: §6" + playList.getId());
    }

    private String getRandomString(List<String> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    public void updateTime(float timeToPlay) {
        ChangeAudioTimePacket packet = new ChangeAudioTimePacket(timeToPlay, id);
        sendPacketToListeners(packet);
    }

    public void updateLocation(SerializableLocation location) {
        spatialInfo.location = location;
        UpdateAudioLocationPacket packet = new UpdateAudioLocationPacket(this, location);
        sendPacketToListeners(packet);
    }

    public void stop() {
        for (UUID id : new LinkedList<>(currentListeners)) {
            User user = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(id);
            user.stopAudio(this);
        }
    }

    private void sendPacketToListeners(Packet packet) {
        for (UUID id : new LinkedList<>(currentListeners)) {
            User user = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(id);
            user.sendPacket(packet);
        }
    }

    @Getter
    @Setter
    public static class AudioPlayInfo {

        @Expose
        protected String link;
        @Expose
        private double timeToPlay;
        /**
         * Fade in ms
         */
        @Expose
        private int fadeIn;
        /**
         * Fade in ms
         */
        @Expose
        private int fadeOut;

    }

    @Getter
    public static class AudioSpatialInfo {

        @Expose
        protected SerializableLocation location;
        @Expose
        private final double maxVoiceDistance;
        @Expose
        private final String distanceModel;
        @Expose
        private final double refDistance;
        @Expose
        private final double rolloffFactor;

        public AudioSpatialInfo(SerializableLocation location, double maxVoiceDistance) {
            this(location, maxVoiceDistance, "exponential", maxVoiceDistance / 3.2, maxVoiceDistance / 4);
        }

        public AudioSpatialInfo(SerializableLocation location, double maxVoiceDistance, String distanceModel, double refDistance, double rolloffFactor) {
            this.location = location;
            this.maxVoiceDistance = maxVoiceDistance;
            this.distanceModel = distanceModel;
            this.refDistance = refDistance;
            this.rolloffFactor = rolloffFactor;
        }

    }

}