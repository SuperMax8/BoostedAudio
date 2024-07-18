package fr.supermax_8.boostedaudio.core.websocket;

import lombok.Getter;

@Getter
public class TurnConfig {

    private final String sharedSecret;
    private final String url;
    private final int expirationTimeInMinutes;

    public TurnConfig(String sharedSecret, String url, int expirationTimeInMinutes) {
        this.sharedSecret = sharedSecret;
        this.url = url;
        this.expirationTimeInMinutes = expirationTimeInMinutes;
    }


}