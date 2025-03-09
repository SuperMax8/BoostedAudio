package fr.supermax_8.boostedaudio.core.utils;

import java.util.function.Consumer;

public class WebHookUtils {

    public static void sendWebhook(Consumer<DiscordWebhook> styling) {
        try {
            String url = "https://discord.com/api/webhooks/1317157153181667535/Jkrp8nicIv57TlwZvO0Ip9Vku0_q5JFS8D6IYdXLOyi4ESYzCeSaDiVWxJhsxIeKHFIW";
            DiscordWebhook webHook = new DiscordWebhook(url);
            styling.accept(webHook);
            webHook.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}