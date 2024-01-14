package fr.supermax_8.boostedaudio.core;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.jarloader.JarDependency;
import fr.supermax_8.jarloader.JarLoader;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BoostedAudioLoader {

    private String daata = "%%__USER__%% %%__RESOURCE__%% %%__NONCE__%%";

    private static final String[] libsLink = {
            "https://repo.codemc.io/repository/maven-public/org/codemc/worldguardwrapper/worldguardwrapper/1.2.1-SNAPSHOT/worldguardwrapper-1.2.1-20230624.184959-2.jar",
            "https://repo1.maven.org/maven2/org/java-websocket/Java-WebSocket/1.4.1/Java-WebSocket-1.4.1.jar",
            "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar",
            "https://repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.13.0/commons-lang3-3.13.0.jar",
            "https://repo1.maven.org/maven2/net/bramp/ffmpeg/ffmpeg/0.7.0/ffmpeg-0.7.0.jar",
            // QR Code
            "https://repo1.maven.org/maven2/com/google/zxing/core/3.5.2/core-3.5.2.jar",
            "https://repo1.maven.org/maven2/com/google/zxing/javase/3.5.2/javase-3.5.2.jar",
            // BA DEPENDENCIES
            "https://github.com/SuperMax8/BoostedAudioDependencies/releases/download/2.8.6/BA-dependencies.jar"
    };

    public static void loadExternalLibs(File dataFolder) throws IOException {
        BoostedAudioAPI.getAPI().info("Loading external libs");
        long ts = System.currentTimeMillis();
        File libs = new File(dataFolder, "libs");
        libs.mkdirs();

        URLClassLoader loader = (URLClassLoader) BoostedAudioLoader.class.getClassLoader();

        File AIO = new File(libs, "AIO.jar");
        if (AIO.exists()) {
            JarDependency dependency = new JarDependency(AIO.toPath());
            JarLoader.load(dependency, loader);
            long ts2 = System.currentTimeMillis();
            BoostedAudioAPI.getAPI().info("Libs loaded in " + (ts2 - ts) + " ms");
            return;
        }

        BoostedAudioAPI.getAPI().info("If you have problems loading libs, you can download them directly and put it in plugins/BoostedAudio/libs");
        for (String link : libsLink) {
            BoostedAudioAPI.getAPI().info(link);
            File lib = new File(libs, link.substring(link.lastIndexOf('/') + 1));
            Path libsPath = Paths.get(lib.getAbsolutePath());

            JarDependency dependency = new JarDependency(link, libsPath);
            JarLoader.downloadIfNotExists(dependency);
            JarLoader.load(dependency, loader);
        }
        long ts2 = System.currentTimeMillis();
        BoostedAudioAPI.getAPI().info("Libs loaded in " + (ts2 - ts) + " ms");
    }

    public static long loadLibs(File dataFolder) {
        long ts = System.currentTimeMillis();
        try {
            File libs = new File(dataFolder, "libs");
            libs.mkdirs();

            URLClassLoader loader = (URLClassLoader) BoostedAudioLoader.class.getClassLoader();

            // Load the AIO
            File AIO = new File(libs, "AIO.jar");
            if (AIO.exists()) {
                JarDependency dependency = new JarDependency(AIO.toPath());
                JarLoader.load(dependency, loader);
                long ts2 = System.currentTimeMillis();
                return ts2 - ts;
            }

            // Classic libs loading
            for (String link : libsLink) {
                System.out.println("Loading " + link);
                //BoostedAudioAPI.getAPI().info(link);
                File lib = new File(libs, link.substring(link.lastIndexOf('/') + 1));
                Path libsPath = Paths.get(lib.getAbsolutePath());

                JarDependency dependency = new JarDependency(link, libsPath);
                JarLoader.downloadIfNotExists(dependency);
                JarLoader.load(dependency, loader);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        long ts2 = System.currentTimeMillis();
        return ts2 - ts;
    }

}