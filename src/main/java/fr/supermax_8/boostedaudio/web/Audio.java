package fr.supermax_8.boostedaudio.web;

import fr.supermax_8.boostedaudio.utils.SerializableLocation;
import org.wildfly.common.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Audio {

    private final List<String> links;

    @Nullable
    private final AudioSpatialInfo spatialInfo;

    private final UUID id;

    private final int fadeIn;
    private final int fadeOut;
    private final boolean loop;

    public Audio(String links, AudioSpatialInfo spatialInfo, UUID id, int fadeIn, int fadeOut, boolean loop) {
        this(Collections.singletonList(links), spatialInfo, id, fadeIn, fadeOut, loop);
    }

    public Audio(List<String> links, AudioSpatialInfo spatialInfo, UUID id, int fadeIn, int fadeOut, boolean loop) {
        this.links = links;
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
        // Get random link from liste
        return links.get(new Random().nextInt(links.size()));
    }

    public List<String> getLinks() {
        return links;
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
            this(location, maxVoiceDistance, "exponential", maxVoiceDistance / 3.2, maxVoiceDistance / 4);
        }

        public AudioSpatialInfo(SerializableLocation location, double maxVoiceDistance, String distanceModel, double refDistance, double rolloffFactor) {
            this.location = location;
            this.maxVoiceDistance = maxVoiceDistance;
            this.distanceModel = distanceModel;
            this.refDistance = refDistance;
            this.rolloffFactor = rolloffFactor;
        }

        public double getMaxVoiceDistance() {
            return maxVoiceDistance;
        }

        public double getRefDistance() {
            return refDistance;
        }

        public double getRolloffFactor() {
            return rolloffFactor;
        }

        public SerializableLocation getLocation() {
            return location;
        }

        public String getDistanceModel() {
            return distanceModel;
        }

    }


}