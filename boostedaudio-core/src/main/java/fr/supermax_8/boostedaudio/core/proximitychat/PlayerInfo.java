package fr.supermax_8.boostedaudio.core.proximitychat;

import fr.supermax_8.boostedaudio.core.utils.SerializableLocation;
import org.wildfly.common.annotation.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class PlayerInfo {

    private final List<UUID> peers;
    private final SerializableLocation location;


    public PlayerInfo(SerializableLocation location) {
        this(new LinkedList<>(), location);
    }

    public PlayerInfo(List<UUID> peers, SerializableLocation location) {
        this.peers = peers;
        this.location = location;
    }



    public SerializableLocation getLocation() {
        return location;
    }

    @NotNull
    public List<UUID> getPeers() {
        return peers;
    }


}