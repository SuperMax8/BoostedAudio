package fr.supermax_8.boostedaudio;

import org.bukkit.configuration.file.FileConfiguration;

public class BoostedAudioConfiguration {

    private boolean debugMode;

    private int port;

    private String hostName;

    private float maxVoiceDistance;

    public BoostedAudioConfiguration() {
        load();
    }

    private void load() {
        BoostedAudio.getInstance().saveDefaultConfig();
        FileConfiguration config = BoostedAudio.getInstance().getConfig();

        debugMode = config.getBoolean("debugMode");

        port = config.getInt("port", 8080);
        hostName = config.getString("hostName", "localhost");

        maxVoiceDistance = (float) config.getDouble("maxVoiceDistance", 30);
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public int getPort() {
        return port;
    }

    public String getHostName() {
        return hostName;
    }

    public float getMaxVoiceDistance() {
        return maxVoiceDistance;
    }

}