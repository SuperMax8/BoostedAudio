package fr.supermax_8.boostedaudio.core.websocket;

public class ServerInfo {

    private final double maxDistance;
    private final float rolloffFactor;
    private final float refDistance;
    private final String distanceModel;
    private final String playerId;

    public ServerInfo(double maxDistance, float rolloffFactor, float refDistance, String distanceModel, String playerId) {
        this.maxDistance = maxDistance;
        this.rolloffFactor = rolloffFactor;
        this.refDistance = refDistance;
        this.distanceModel = distanceModel;
        this.playerId = playerId;
    }

}