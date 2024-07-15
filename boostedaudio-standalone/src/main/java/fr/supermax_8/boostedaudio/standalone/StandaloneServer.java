package fr.supermax_8.boostedaudio.standalone;

import fr.supermax_8.boostedaudio.sharedutils.BoostedAudioLoader;
import fr.supermax_8.boostedaudio.sharedutils.JavaUtils;
import fr.supermax_8.boostedaudio.sharedutils.jarloader.JarLoader;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;

public class StandaloneServer {

    public static void main(String[] args) throws IOException {
        File dataFolder = JavaUtils.getCurrentDir();
        File boostedAudioJar = null;
        for (File file : dataFolder.listFiles()) {
            String fileName = file.getName();
            if (fileName.toLowerCase().contains("boostedaudio") && fileName.endsWith(".jar"))
                boostedAudioJar = file;
        }
        if (boostedAudioJar == null) {
            System.out.println("No boostedaudio jar provided, put a jar of the plugin in this directory !");
            return;
        }

        System.out.println("Loading boostedaudio jar: " + boostedAudioJar.getName());

        URLClassLoader loader = BoostedAudioLoader.loadExternalLibs(dataFolder);
        JarLoader.load(boostedAudioJar.toPath(), loader);

        try {
            Class<?> serverClass = loader.loadClass("fr.supermax_8.boostedaudio.core.BoostedAudioStandalone");
            serverClass.getConstructor(File.class).newInstance(dataFolder);
        } catch (Exception e) {
            System.out.println("Error while starting the server");
            e.printStackTrace();
        }
    }

}