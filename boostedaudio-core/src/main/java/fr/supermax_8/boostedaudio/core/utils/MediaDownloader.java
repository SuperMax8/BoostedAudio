package fr.supermax_8.boostedaudio.core.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.File;

public class MediaDownloader {


    public static String download(String mediaLink, String format, File directory) {
        System.out.println("Starting getLink...");
        long ts1 = System.currentTimeMillis();
        JsonObject object = HttpUtils.httpGet("https://loader.to/ajax/download.php?format=" + format + "&url=" + mediaLink);
        long ts2 = System.currentTimeMillis();
        String id = object.get("id").getAsString();
        System.out.println("Link got in " + (ts2 - ts1) + " ms Id: " + id);

        int i = 0;
        while (i < 1000) {
            try {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String result = progress(directory, id);
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
        try {
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
        } catch (Exception e) {
            System.out.println("Error while dl progress send a screen of this and the error to the dev of this software please" + object.toString());
            e.printStackTrace();
        }
        return null;
    }


}