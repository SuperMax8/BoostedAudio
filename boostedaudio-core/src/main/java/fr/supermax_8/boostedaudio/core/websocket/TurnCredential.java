package fr.supermax_8.boostedaudio.core.websocket;

import lombok.Getter;

@Getter
public class TurnCredential {

    private final String username;
    private final String credential;
    private final long expirationTsMs;


    public TurnCredential(String username, String credential, long expirationTsMs) {
        this.username = username;
        this.credential = credential;
        this.expirationTsMs = expirationTsMs;
    }

}