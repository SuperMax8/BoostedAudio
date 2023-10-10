package fr.supermax_8.boostedaudio.utils;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.BoostedAudioLoader;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import javax.net.ssl.SSLContext;
import javax.sound.sampled.*;
import java.io.*;

public class FileUtils {

    public static FFmpeg ffmpeg;
    public static FFprobe ffprobe;

    static {
        File ffmpegDir = new File(BoostedAudioLoader.getInstance().getDataFolder(), "ffmpeg");
        try {
            ffmpeg = new FFmpeg(ffmpegDir.listFiles((d, s) -> s.contains("ffmpeg"))[0].getAbsolutePath());
            ffprobe = new FFprobe(ffmpegDir.listFiles((d, s) -> s.contains("ffprobe"))[0].getAbsolutePath());
        } catch (Exception e) {
            if (BoostedAudio.getInstance().getConfiguration().isDebugMode()) {
                System.out.println("You don't have ffmpeg installed");
                e.printStackTrace();
            }
        }
    }

    public static void replaceInFile(File inputFile, String toReplace, String replacementText) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                content.append(line).append(System.lineSeparator());

            reader.close();

            String modifiedContent = content.toString().replace(toReplace, replacementText);

            BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile));
            writer.write(modifiedContent);
            writer.close();
        } catch (Exception e) {
            if (BoostedAudio.getInstance().getConfiguration().isDebugMode()) e.printStackTrace();
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