package fr.supermax_8.boostedaudio.core.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.File;

public class MediaDownloader {

    // Old "https://loader.to/ajax/download.php?format=" + format + "&url=" + mediaLink
    // In 05/12/2024 https://p.oceansaver.in/ajax/download.php?format=wav&url=https://www.youtube.com/watch?v=YYX9Nup2MCs

    public static String download(String mediaLink, String format, File directory) {
        System.out.println("Starting getLink...");
        long ts1 = System.currentTimeMillis();
        JsonObject object = HttpUtils.httpGet("https://p.oceansaver.in/ajax/download.php?format=" + format + "&url=" + mediaLink);
        long ts2 = System.currentTimeMillis();
        String id = object.get("id").getAsString();
        System.out.println("Link got in " + (ts2 - ts1) + " ms Id: " + id);

        int i = 0;
        while (i < 1000) {
            try {
                try {
                    Thread.sleep((long) MathUtils.randomBetween(950, 1500));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String result;
                try {
                    result = progress(directory, id);
                } catch (Exception e) {
                    System.out.println("Error while dl progress send a screen of this and the error to the dev of this software please" + object.toString());
                    e.printStackTrace();
                    result = null;
                    i += 10;
                    try {
                        Thread.sleep((long) MathUtils.randomBetween(5500, 20000));
                    } catch (InterruptedException xe) {
                        e.printStackTrace();
                    }
                }
                if (result != null) return result;
                i++;
            } catch (Exception e) {
                System.out.println("Download failed Id: " + id);
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private static String progress(File directory, String id) {
        JsonObject object = HttpUtils.httpGet("https://p.oceansaver.in/ajax/progress.php?id=" + id);
        JsonElement text = object.get("text");
        if (text.isJsonNull()) return null;
        String status = text.getAsString();
        System.out.println("Downloading... Status: " + status);
        switch (status) {
            case "Finished" -> {
                String downloadUrl = object.get("download_url").getAsString();
                System.out.println(downloadUrl + " Starting download...");
                return HttpUtils.downloadAudioSafely(downloadUrl, directory.getAbsolutePath());
            }
            case "Error" -> {
                System.out.println("Error while downloading a file, maybe it was too big ?");
                return "error";
            }
        }
        return null;
    }


}