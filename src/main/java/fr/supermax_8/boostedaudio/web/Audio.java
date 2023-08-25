package fr.supermax_8.boostedaudio.web;

import fr.supermax_8.boostedaudio.utils.SerializableLocation;
import org.wildfly.common.annotation.Nullable;

import java.util.UUID;

public class Audio {

    private final String link;

    @Nullable
    private final SerializableLocation location;

    private final UUID id;

    private final int fadeIn;
    private final int fadeOut;

    public Audio(String link, SerializableLocation location, UUID id, int fadeIn, int fadeOut) {
        this.link = link;
        this.location = location;
        this.id = id;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
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

    public SerializableLocation getLocation() {
        return location;
    }

    public UUID getId() {
        return id;
    }

}