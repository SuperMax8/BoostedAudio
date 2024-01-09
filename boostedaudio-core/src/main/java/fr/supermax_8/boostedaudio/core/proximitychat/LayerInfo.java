package fr.supermax_8.boostedaudio.core.proximitychat;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
public class LayerInfo {

    private final String layerId;

    // PlayerId : PlayerInfo
    private final Map<UUID, PlayerInfo> playersInfo;


    public LayerInfo(Map<UUID, PlayerInfo> playersInfo, String layerId) {
        this.playersInfo = playersInfo;
        this.layerId = layerId;
    }


}