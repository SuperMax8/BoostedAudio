package fr.supermax_8.boostedaudio.core.proximitychat;

import fr.supermax_8.boostedaudio.core.utils.SerializableLocation;
import lombok.Getter;
import org.wildfly.common.annotation.NotNull;

import java.util.*;

public class PlayerInfo {

    private final Set<UUID> peers;
    @Getter
    private final SerializableLocation location;
    @Getter
    private final boolean muted;

    public PlayerInfo(SerializableLocation location, boolean muted) {
        this(new HashSet<>(), location, muted);
    }

    public PlayerInfo(Set<UUID> peers, SerializableLocation location, boolean muted) {
        this.peers = peers;
        this.location = location;
        this.muted = muted;
    }


    @NotNull
    public Set<UUID> getPeers() {
        return peers;
    }


    @Override
    public String toString() {
        return "PlayerInfo{" +
                "peers=" + peers +
                ", location=" + location +
                ", muted=" + muted +
                '}';
    }


}