package fr.supermax_8.boostedaudio.core.utils;

import com.google.gson.JsonObject;

import java.io.File;

public class MediaDownloader {


    public static String download(String mediaLink, String format, File directory) {
        JsonObject object = HttpUtils.httpGet("https://ab.cococococ.com/ajax/download.php?format=" + format + "&url=" + mediaLink);
        String id = object.get("id").getAsString();

        int i = 0;
        while (i < 1000) {
            String result = progress(directory, id);
            if (result != null) return result;
            i++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {

            } catch (Exception e) {
            }
        }
        return null;
    }

    private static String progress(File directory, String id) {
        JsonObject object = HttpUtils.httpGet("https://p.oceansaver.in/ajax/progress.php?id=" + id);
        if (object.get("text").getAsString().equals("Finished")) {
            String downloadUrl = object.get("download_url").getAsString();
            System.out.println(downloadUrl + " Starting download...");
            return HttpUtils.downloadAudioSafely(downloadUrl, directory.getAbsolutePath());
        }
        return null;
    }


}