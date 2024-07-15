package fr.supermax_8.boostedaudio.core.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MojangAPI {


    public static String getUsernameFromUUID(String uuid) throws Exception {
        String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            connection.disconnect();

            JsonElement json = new JsonParser().parse(content.toString());
            return json.getAsJsonObject().get("name").getAsString();
        } else {
            throw new Exception("Failed to retrieve data from Mojang API. Response code: " + responseCode);
        }
    }


}