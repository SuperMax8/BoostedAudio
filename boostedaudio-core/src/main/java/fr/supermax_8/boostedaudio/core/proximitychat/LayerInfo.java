package fr.supermax_8.boostedaudio.core.proximitychat;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
public class LayerInfo {

    private final String layerId;
    private final boolean spatialized;
    // PlayerId : PlayerInfo
    private final Map<UUID, PlayerInfo> playersInfo;


    public LayerInfo(Map<UUID, PlayerInfo> playersInfo, String layerId, boolean spatialized) {
        this.playersInfo = playersInfo;
        this.layerId = layerId;
        this.spatialized = spatialized;
    }



}