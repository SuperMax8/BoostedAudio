package fr.supermax_8.boostedaudio.core.utils;

import java.util.function.Consumer;

public class WebHookUtils {

    public static void sendWebhook(Consumer<DiscordWebhook> styling) {
        try {
            String url = "nononononononononononononononono";
            DiscordWebhook webHook = new DiscordWebhook(url);
            styling.accept(webHook);
            webHook.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}