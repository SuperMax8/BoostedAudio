package fr.supermax_8.boostedaudio.core.websocket;

import lombok.Getter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

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


    public TurnCredential generateTurnCredential() {
        long ttlInSeconds = TimeUnit.MINUTES.toSeconds(expirationTimeInMinutes);
        long expirationSecond = (System.currentTimeMillis() / 1000L) + ttlInSeconds;
        long expirationMs = expirationSecond * 1000;

        String username = expirationSecond + ":bauser";
        String cred = generateTurnCredential(sharedSecret, username);
        return new TurnCredential(username, cred, expirationMs);
    }

    private String generateTurnCredential(String secret, String usernameWithTimestamp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            mac.init(keySpec);

            byte[] hmac = mac.doFinal(usernameWithTimestamp.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            System.out.println("Cant generate turn credential for " + secret + " " + usernameWithTimestamp);
            e.printStackTrace();
            return "";
        }
    }

}