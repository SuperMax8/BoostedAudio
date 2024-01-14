package fr.supermax_8.boostedaudio.core.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class UpdateChecker {

    private final int resourceId;

    public UpdateChecker(int resourceId) {
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<String> consumer) {
        CompletableFuture.runAsync(() -> {
            String apiUrl = "https://api.spigotmc.org/simple/0.2/index.php?action=getResource&id=" + this.resourceId;
            try (InputStream inputStream = new URL(apiUrl).openStream()) {
                InputStreamReader reader = new InputStreamReader(inputStream);
                JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
                String version = jsonObject.get("current_version").getAsString();
                consumer.accept(version);
            } catch (IOException exception) {
                BoostedAudioAPI.getAPI().info("Unable to check for updates: " + exception.getMessage());
            }
        });
    }

}