package fr.supermax_8.boostedaudio.core.proximitychat;

import fr.supermax_8.boostedaudio.core.utils.SerializableLocation;
import org.wildfly.common.annotation.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class PlayerInfo {

    private final List<UUID> peers;
    private final SerializableLocation location;
    private final boolean muted;

    public PlayerInfo(SerializableLocation location, boolean muted) {
        this(new LinkedList<>(), location, muted);
    }

    public PlayerInfo(List<UUID> peers, SerializableLocation location, boolean muted) {
        this.peers = peers;
        this.location = location;
        this.muted = muted;
    }


    public SerializableLocation getLocation() {
        return location;
    }

    @NotNull
    public List<UUID> getPeers() {
        return peers;
    }

    public boolean isMuted() {
        return muted;
    }

}