package fr.supermax_8.boostedaudio.core.utils;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class FFmpegUtils {

    public static FFmpeg ffmpeg;
    public static FFprobe ffprobe;
    public static ConcurrentHashMap<String, Double> durationCache = new ConcurrentHashMap<>();

    static {
        try {
            BoostedAudioAPI.getAPI().info("§7Initializing ffmpeg...");
            File ffmpegDir = new File(BoostedAudioAPI.getAPI().getConfiguration().getDataFolder(), "ffmpeg");
            String ffmpegPath = ffmpegDir.listFiles((d, s) -> s.contains("ffmpeg"))[0].getAbsolutePath();
            String ffmprobe = ffmpegDir.listFiles((d, s) -> s.contains("ffprobe"))[0].getAbsolutePath();
            try {
                ffmpeg = new FFmpeg(ffmpegPath);
                ffprobe = new FFprobe(ffmprobe);
                BoostedAudioAPI.getAPI().info("§2FFmpeg initialized!");
            } catch (Throwable e) {
                BoostedAudioAPI.getAPI().info("§cError with ffmpeg files");
                e.printStackTrace();
            }
        } catch (Throwable e) {
            BoostedAudioAPI.getAPI().debug("You don't have ffmpeg installed, guide to install it -> https://supermax-8.gitbook.io/boostedaudio/basics/install-ffmpeg-in-game-audio-volume-modification");
            if (BoostedAudioAPI.getAPI().getConfiguration().isDebugMode()) {
                e.printStackTrace();
            }
        }
    }

    public static void warningffmpeg() {
        if (ffmpeg == null || ffprobe == null) {
            BoostedAudioAPI.getAPI().info("§cYou need to install ffmpeg to use this feature! Guide to install it -> https://supermax-8.gitbook.io/boostedaudio/basics/install-ffmpeg-in-game-audio-volume-modification");
        }
    }

    public static void adjustGain(String inputFilePath, String outputFilePath, float gainChange) {
        warningffmpeg();
        try {
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(inputFilePath)
                    .setAudioFilter("volume=" + gainChange + "dB")
                    .addOutput(outputFilePath)
                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

            executor.createJob(builder).run();
        } catch (Exception e) {
            e.printStackTrace();
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

    public static double getAudioDurationEx(String audioUrl) throws Exception {
        warningffmpeg();
        Double duration = durationCache.get(audioUrl);
        if (duration != null) return duration;

        // Use FFprobe to probe the file
        FFmpegProbeResult probeResult = ffprobe.probe(audioUrl);

        // Extract duration (in seconds)
        double d = probeResult.getFormat().duration;
        durationCache.put(audioUrl, d);
        return d;
    }

}