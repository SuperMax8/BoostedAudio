package fr.supermax_8.boostedaudio.spigot.utils;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.*;

public class FileUtils {

    public static FFmpeg ffmpeg;
    public static FFprobe ffprobe;

    static {
        File ffmpegDir = new File(BoostedAudioSpigot.getInstance().getDataFolder(), "ffmpeg");
        try {
            String ffmpegPath = ffmpegDir.listFiles((d, s) -> s.contains("ffmpeg"))[0].getAbsolutePath();
            String ffmprobe = ffmpegDir.listFiles((d, s) -> s.contains("ffprobe"))[0].getAbsolutePath();
            try {
                ffmpeg = new FFmpeg(ffmpegPath);
                ffprobe = new FFprobe(ffmprobe);
            } catch (Exception e) {
                BoostedAudioAPI.getAPI().info("§cError with ffmpeg files");
                e.printStackTrace();
            }
        } catch (Exception e) {
            BoostedAudioAPI.getAPI().debug("You don't have ffmpeg installed");
        }
    }

    public static void adjustGain(String inputFilePath, String outputFilePath, float gainChange) {
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

    private static double getModifiedSample(byte[] audioData, int i, double gainFactor) {
        short sample = (short) ((audioData[i + 1] << 8) | (audioData[i] & 0xff));

        // Appliquer le gain au sample
        double modifiedSample = sample * gainFactor;

        // S'assurer que le sample reste dans la plage de valeurs autorisées (-32768 à 32767)
        if (modifiedSample > Short.MAX_VALUE) {
            modifiedSample = Short.MAX_VALUE;
        } else if (modifiedSample < Short.MIN_VALUE) {
            modifiedSample = Short.MIN_VALUE;
        }
        return modifiedSample;
    }


}