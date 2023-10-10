package fr.supermax_8.boostedaudio;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import fr.supermax_8.boostedaudio.utils.FileUtils;
import fr.supermax_8.jarloader.JarDependency;
import fr.supermax_8.jarloader.JarLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BoostedAudioLoader extends JavaPlugin {

    private static BoostedAudioLoader instance;

    private String daata = "%%__USER__%% %%__RESOURCE__%% %%__NONCE__%%";

    private BoostedAudio boostedAudio;

    private static final String[] libsLink = {
            "https://repo.codemc.io/repository/maven-public/org/codemc/worldguardwrapper/worldguardwrapper/1.2.1-SNAPSHOT/worldguardwrapper-1.2.1-20230624.184959-2.jar",
            "https://repo1.maven.org/maven2/org/java-websocket/Java-WebSocket/1.5.0/Java-WebSocket-1.5.0.jar",
            "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar",
            "https://repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.13.0/commons-lang3-3.13.0.jar",
            "https://repo1.maven.org/maven2/net/bramp/ffmpeg/ffmpeg/0.7.0/ffmpeg-0.7.0.jar",
/*            "https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk18on/1.76/bcprov-jdk18on-1.76.jar",
            "https://repo1.maven.org/maven2/org/bouncycastle/bcpkix-jdk18on/1.76/bcpkix-jdk18on-1.76.jar",*/
            // UnderTow
            "https://github.com/SuperMax8/UnderTowAIO/releases/download/2.3.8/UnderTowAIO-2.3.8.jar"
            /*"https://repo1.maven.org/maven2/org/jboss/xnio/xnio-api/3.8.8.Final/xnio-api-3.8.8.Final.jar",
            "https://repo1.maven.org/maven2/org/jboss/xnio/xnio-nio/3.8.8.Final/xnio-nio-3.8.8.Final.jar",
            "https://repo1.maven.org/maven2/org/jboss/logging/jboss-logging/3.4.3.Final/jboss-logging-3.4.3.Final.jar",
            "https://repo1.maven.org/maven2/org/jboss/threads/jboss-threads/3.5.0.Final/jboss-threads-3.5.0.Final.jar",
            "https://repo1.maven.org/maven2/io/undertow/undertow-core/2.3.8.Final/undertow-core-2.3.8.Final.jar",*/
    };

    @Override
    public void onEnable() {
        instance = this;
        try {
            loadExternalLibs();
        } catch (IOException e) {
            if (BoostedAudio.getInstance().getConfiguration().isDebugMode()) e.printStackTrace();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                int pluginId = 19857;
                Metrics metrics = new Metrics(BoostedAudioLoader.getInstance(), pluginId);
                metrics.addCustomChart(new Metrics.SimplePie("sucessful_setup", () -> String.valueOf(boostedAudio.isSucessfulSetup())));
                metrics.addCustomChart(new Metrics.SimplePie("ffmpeg_setuped", () -> String.valueOf(FileUtils.ffmpeg != null)));
                metrics.addCustomChart(new Metrics.SingleLineChart("players_connected_to_audio_panel", () ->
                        BoostedAudio.getInstance().getWebSocketServer().manager.getUsers().size()));
                metrics.addCustomChart(new Metrics.SimplePie("nbspeakers", () -> intMetricToEzReadString(BoostedAudio.getInstance().getAudioManager().getSpeakerManager().speakers.size())));
                metrics.addCustomChart(new Metrics.SimplePie("nbregions", () -> {
                    try {
                        return intMetricToEzReadString(BoostedAudio.getInstance().getAudioManager().getRegionManager().getAudioRegions().size());
                    } catch (Exception e) {
                        return "No regions";
                    }
                }));
            }
        }.runTaskLater(instance, 20 * 5);

        boostedAudio = new BoostedAudio();
        boostedAudio.onEnable();
    }

    private String intMetricToEzReadString(int metrics) {
        if (metrics >= 1000) return "> 1000 (WOW)";
        if (metrics >= 200) return "> 200";
        if (metrics >= 100) return "> 100";
        if (metrics >= 30) return "> 30";
        if (metrics >= 10) return "> 10";
        if (metrics >= 5) return "> 5";
        return "0";
    }

    @Override
    public void onDisable() {
        boostedAudio.onDisable();
    }

    public static BoostedAudioLoader getInstance() {
        return instance;
    }

    private void loadExternalLibs() throws IOException {
        System.out.println("Loading external libs");
        long ts = System.currentTimeMillis();
        File libs = new File(getDataFolder(), "libs");
        libs.mkdirs();

        URLClassLoader loader = (URLClassLoader) getClassLoader();
        File AIO = new File(libs, "AIO.jar");
        if (AIO.exists()) {
            JarDependency dependency = new JarDependency(AIO.toPath());
            JarLoader.load(dependency, loader);
            long ts2 = System.currentTimeMillis();
            System.out.println("Libs loaded in " + (ts2 - ts) + " ms");
            return;
        }

        System.out.println("If you have problems loading libs, you can download them directly and put it in plugins/BoostedAudio/libs");
        for (String link : libsLink) {
            System.out.println(link);
            File lib = new File(libs, link.substring(link.lastIndexOf('/') + 1));
            Path libsPath = Paths.get(lib.getAbsolutePath());

            JarDependency dependency = new JarDependency(link, libsPath);
            JarLoader.downloadIfNotExists(dependency);
            JarLoader.load(dependency, loader);
        }
        long ts2 = System.currentTimeMillis();
        System.out.println("Libs loaded in " + (ts2 - ts) + " ms");
    }


}