package fr.supermax_8.boostedaudio.sharedutils;

import fr.supermax_8.boostedaudio.sharedutils.jarloader.JarDependency;
import fr.supermax_8.boostedaudio.sharedutils.jarloader.JarLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BoostedAudioLoader {

    private String daata = "%%__USER__%% %%__RESOURCE__%% %%__NONCE__%%";

    private static final String[] libsLink = {
//            "https://repo.codemc.io/repository/maven-public/org/codemc/worldguardwrapper/worldguardwrapper/1.2.1-SNAPSHOT/worldguardwrapper-1.2.1-20230624.184959-2.jar",
            "https://repo1.maven.org/maven2/org/java-websocket/Java-WebSocket/1.4.1/Java-WebSocket-1.4.1.jar",
            "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar",
            "https://repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.13.0/commons-lang3-3.13.0.jar",
/*            "https://repo1.maven.org/maven2/net/bramp/ffmpeg/ffmpeg/0.8.0/ffmpeg-0.8.0.jar",*/
            // QR Code
            "https://repo1.maven.org/maven2/com/google/zxing/core/3.5.2/core-3.5.2.jar",
            "https://repo1.maven.org/maven2/com/google/zxing/javase/3.5.2/javase-3.5.2.jar",
            // BA DEPENDENCIES
            "https://github.com/SuperMax8/BoostedAudioDependencies/releases/download/2.17.0/BA-dependencies-jar-with-dependencies.jar",
    };

    public static URLClassLoader loadExternalLibs(File dataFolder) throws IOException {
        System.out.println("Loading external libs");
        long ts = System.currentTimeMillis();
        File libs = new File(dataFolder, "libs");
        libs.mkdirs();

        ClassLoader loader = BoostedAudioLoader.class.getClassLoader();
        if (!(loader instanceof URLClassLoader)) {
            loader = new URLClassLoader(new URL[]{});
            Thread.currentThread().setContextClassLoader(loader);
        }
        URLClassLoader loader1 = (URLClassLoader) loader;

        System.out.println("If you have problems loading libs, you can download them directly and put it in plugins/BoostedAudio/libs");
        for (String link : libsLink) {
            System.out.println(link);
            File lib = new File(libs, link.substring(link.lastIndexOf('/') + 1));
            Path libsPath = Paths.get(lib.getAbsolutePath());

            JarDependency dependency = new JarDependency(link, libsPath);
            JarLoader.downloadIfNotExists(dependency);
            JarLoader.load(dependency, loader1);
        }

        long ts2 = System.currentTimeMillis();
        System.out.println("Libs loaded in " + (ts2 - ts) + " ms");
        return loader1;
    }

}