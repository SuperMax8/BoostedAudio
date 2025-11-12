package fr.supermax_8.boostedaudio.core.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class FFmpegUtils {

    public static File ffmpeg;
    public static File ffprobe;
    public static ConcurrentHashMap<String, Double> durationCache = new ConcurrentHashMap<>();

    static {
        try {
            BoostedAudioAPI.getAPI().info("§7Initializing ffmpeg...");
            File ffmpegDir = new File(BoostedAudioAPI.getAPI().getConfiguration().getDataFolder(), "ffmpeg");
            String ffmpegPath = ffmpegDir.listFiles((d, s) -> s.contains("ffmpeg"))[0].getAbsolutePath();
            String ffmprobe = ffmpegDir.listFiles((d, s) -> s.contains("ffprobe"))[0].getAbsolutePath();
            ffmpeg = new File(ffmpegPath);
            ffprobe = new File(ffmprobe);
            BoostedAudioAPI.getAPI().info("§2FFmpeg initialized!");
        } catch (Throwable e) {
            BoostedAudioAPI.getAPI().debug("You don't have ffmpeg installed, guide to install it -> https://supermax-8.gitbook.io/boostedaudio/basics/install-ffmpeg-in-game-audio-volume-modification");
        }
    }

    public static void warningffmpeg() {
        if (ffmpeg == null || ffprobe == null) {
            BoostedAudioAPI.getAPI().info("§cYou need to install ffmpeg to use this feature! Guide to install it -> https://supermax-8.gitbook.io/boostedaudio/basics/install-ffmpeg-in-game-audio-volume-modification");
        }
    }

    public static boolean adjustGain(String inputFilePath, String outputFilePath, float gainChangeDb) {
        warningffmpeg();
        List<String> command = new ArrayList<>();
        command.add(ffmpeg.getAbsolutePath());
        command.add("-i");
        command.add(inputFilePath);
        command.add("-af");
        command.add("volume=" + gainChangeDb + "dB");
        command.add("-y"); // Overwrite the output file if it exists
        command.add(outputFilePath);

        BoostedAudioAPI.getAPI().debug("FFMPEG Command execution : " + String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    BoostedAudioAPI.getAPI().debug("FFMPEG: " + line);
                }
            }

            boolean finished = process.waitFor(1, TimeUnit.MINUTES); // Timeout
            if (!finished) {
                process.destroy();
                BoostedAudioAPI.getAPI().debug("FFMPEG Timeout");
                return false;
            }

            int exitCode = process.exitValue();
            if (exitCode == 0) {
                BoostedAudioAPI.getAPI().debug("FFmpeg change gain success : " + exitCode);
                return true;
            } else {
                BoostedAudioAPI.getAPI().info("FFmpeg end with error : " + exitCode);
                return false;
            }

        } catch (IOException e) {
            BoostedAudioAPI.getAPI().info("Error ffmpeg");
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            BoostedAudioAPI.getAPI().debug("FFmpeg interrupt");
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public static CompletableFuture<Double> getAudioDurationAsync(String audioUrl) {
        return CompletableFuture.supplyAsync(() -> getAudioDuration(audioUrl));
    }

    public static double getAudioDuration(String audioUrl) {
        try {
            return getAudioDurationEx(audioUrl);
        } catch (Exception e) {
            if (BoostedAudioAPI.getAPI().getConfiguration().isDebugMode()) {
                BoostedAudioAPI.getAPI().debug("Error while getting audio duration for link: " + audioUrl);
                e.printStackTrace();
            }
            return -1;
        }
    }

    public static double getAudioDurationEx(String audioUrl) {
        warningffmpeg();
        Double duration = durationCache.get(audioUrl);
        if (duration != null) return duration;

        // Extract duration (in seconds)
        double d = computeAudioDuration(audioUrl);
        durationCache.put(audioUrl, d);
        return d;
    }

    public static double computeAudioDuration(String audioUrl) {
        String[] command = {
                ffprobe.getAbsolutePath(),
                "-v", "quiet",
                "-print_format", "json",
                "-show_format",
                "-show_streams",
                audioUrl
        };

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        try {
            Process process = processBuilder.start();

            StringBuilder jsonOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonOutput.append(line);
                }
            }

            boolean finished = process.waitFor(30, TimeUnit.SECONDS); // Timeout
            if (!finished || process.exitValue() != 0) {
                BoostedAudioAPI.getAPI().debug("FFMPEG Timeout");
                return -1;
            }

            JsonObject json = new JsonParser().parse(jsonOutput.toString()).getAsJsonObject();
            JsonObject format = json.getAsJsonObject("format");
            String durationString = format.get("duration").getAsString();

            return Double.parseDouble(durationString);

        } catch (IOException e) {
            BoostedAudioAPI.getAPI().debug("FFMPEG Error");
            e.printStackTrace();
            return -1;
        } catch (InterruptedException e) {
            BoostedAudioAPI.getAPI().debug("FFMPEG interrupt");
            Thread.currentThread().interrupt();
            return -1;
        } catch (Exception e) {
            BoostedAudioAPI.getAPI().debug("FFPROBE PARSING ERROR PROBABLY!!!");
            e.printStackTrace();
            return -1;
        }
    }

}