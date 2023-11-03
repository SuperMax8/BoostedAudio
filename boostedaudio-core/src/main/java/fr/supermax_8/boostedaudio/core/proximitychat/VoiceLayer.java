package fr.supermax_8.boostedaudio.core.proximitychat;

import com.google.gson.annotations.Expose;
import org.wildfly.common.annotation.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class VoiceLayer {
    
    private final boolean audioSpatialized;
    private final int priority;
    @Nullable
    private final Predicate<UUID> playerInLayer;

    private final Set<UUID> playersInside = new HashSet<>();

    @Expose
    private final String id;


    public VoiceLayer(boolean audioSpatialized, int priority, @Nullable Predicate<UUID> playerInLayer, String id) {
        this.audioSpatialized = audioSpatialized;
        this.priority = priority;
        this.playerInLayer = playerInLayer;
        this.id = id;
    }

    public boolean isAudioSpatialized() {
        return audioSpatialized;
    }

    public int getPriority() {
        return priority;
    }

    public Set<UUID> getPlayersInside() {
        return playersInside;
    }


    public Predicate<UUID> getPlayerInLayer() {
        return playerInLayer;
    }

    public String getId() {
        return id;
    }

}