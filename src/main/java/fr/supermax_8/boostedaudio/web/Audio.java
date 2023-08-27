package fr.supermax_8.boostedaudio.web;

import fr.supermax_8.boostedaudio.utils.SerializableLocation;
import org.wildfly.common.annotation.Nullable;

import java.util.UUID;

public class Audio {

    private final String link;

    @Nullable
    private final AudioSpatialInfo spatialInfo;

    private final UUID id;

    private final int fadeIn;
    private final int fadeOut;
    private final boolean loop;

    public Audio(String link, AudioSpatialInfo spatialInfo, UUID id, int fadeIn, int fadeOut, boolean loop) {
        this.link = link;
        this.spatialInfo = spatialInfo;
        this.id = id;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
        this.loop = loop;
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public int getFadeOut() {
        return fadeOut;
    }

    public String getLink() {
        return link;
    }

    public AudioSpatialInfo getSpatialInfo() {
        return spatialInfo;
    }

    public UUID getId() {
        return id;
    }

    public boolean isLoop() {
        return loop;
    }


    public static class AudioSpatialInfo {

        private final SerializableLocation location;

        private final double maxVoiceDistance;

        private final String distanceModel;

        private final double refDistance;

        private final double rolloffFactor;

        public AudioSpatialInfo(SerializableLocation location, double maxVoiceDistance) {
            this(location, maxVoiceDistance, "exponential", maxVoiceDistance / 3, maxVoiceDistance / 4);
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