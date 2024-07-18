package fr.supermax_8.boostedaudio.core.websocket.packets;

import com.google.gson.JsonArray;

public class ServerInfo {

    private final double maxDistance;
    private final float rolloffFactor;
    private final float refDistance;
    private final String distanceModel;
    private final String playerId;
    private final JsonArray iceServers;

    public ServerInfo(double maxDistance, float rolloffFactor, float refDistance, String distanceModel, String playerId, JsonArray iceServers) {
        this.maxDistance = maxDistance;
        this.rolloffFactor = rolloffFactor;
        this.refDistance = refDistance;
        this.distanceModel = distanceModel;
        this.playerId = playerId;
        this.iceServers = iceServers;
    }

}