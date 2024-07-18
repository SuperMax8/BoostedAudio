package fr.supermax_8.boostedaudio.core.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TurnUtils {


    public static String generateTurnUsername(long ttlInSeconds, String baseUsername) {
        long timestamp = (System.currentTimeMillis() / 1000L) + ttlInSeconds;
        return timestamp + ":" + baseUsername;
    }

    public static String generateTurnCredential(String secret, String usernameWithTimestamp) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
        mac.init(keySpec);

        byte[] hmac = mac.doFinal(usernameWithTimestamp.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmac);
    }



}