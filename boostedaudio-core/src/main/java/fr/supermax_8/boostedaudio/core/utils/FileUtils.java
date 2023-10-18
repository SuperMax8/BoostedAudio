package fr.supermax_8.boostedaudio.core.utils;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;

import java.io.*;

public class FileUtils {

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
            if (BoostedAudioAPI.api.getConfiguration().isDebugMode()) e.printStackTrace();
        }
    }


}