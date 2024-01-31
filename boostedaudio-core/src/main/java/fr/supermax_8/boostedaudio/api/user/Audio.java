package fr.supermax_8.boostedaudio.api.user;

import com.google.gson.annotations.Expose;
import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.utils.SerializableLocation;
import fr.supermax_8.boostedaudio.core.websocket.packets.UpdateAudioLocationPacket;
import lombok.Getter;
import org.wildfly.common.annotation.Nullable;

import java.util.*;

@Getter
public class Audio {

    @Expose
    private final List<String> links;
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
    private final boolean syncronous;

    public Audio(String links, AudioSpatialInfo spatialInfo, UUID id, int fadeIn, int fadeOut, boolean loop, boolean syncronous) {
        this(Collections.singletonList(links), spatialInfo, id, fadeIn, fadeOut, loop, syncronous);
    }

    public Audio(List<String> links, AudioSpatialInfo spatialInfo, UUID id, int fadeIn, int fadeOut, boolean loop, boolean syncronous) {
        this.links = links;
        this.spatialInfo = spatialInfo;
        this.id = id;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
        this.loop = loop;
        this.syncronous = syncronous;
    }

    public String getLink() {
        // Get a random link from list
        return links.get(new Random().nextInt(links.size()));
    }

    public void updateLocation(SerializableLocation location) {
        spatialInfo.location = location;
        UpdateAudioLocationPacket packet = new UpdateAudioLocationPacket(this, location);
        for (UUID id : currentListeners) {
            User user = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(id);
            user.sendPacket(packet);
        }
    }

    public void stop() {
        for (UUID id : currentListeners) {
            User user = BoostedAudioAPI.getAPI().getHostProvider().getUsersOnServer().get(id);
            user.stopAudio(this);
        }
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