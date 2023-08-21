package fr.supermax_8.boostedaudio;

import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class BoostedAudioConfiguration {

    private boolean debugMode;
    private String clientLink;
    private boolean autoHost;
    private int autoHostPort;
    private boolean ssl;
    private String keystorePassword;
    private String keystoreFileName;
    private int webSocketPort;
    private String webSocketHostName;
    private float maxVoiceDistance;
    private String connectionMessage;
    private String connectionHoverMessage;
    private String distanceModel;
    private float refDistance;
    private float rolloffFactor;

    public BoostedAudioConfiguration() {
        load();
    }

    private void load() {
        BoostedAudio.getInstance().saveDefaultConfig();
        try {
            File configFile = new File(BoostedAudio.getInstance().getDataFolder(), "config.yml");
            ConfigUpdater.update(BoostedAudio.getInstance(), "config.yml", configFile);
            BoostedAudio.getInstance().reloadConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FileConfiguration config = BoostedAudio.getInstance().getConfig();

        debugMode = config.getBoolean("debugMode");

        clientLink = config.getString("client-link", "http://localhost:8080");
        autoHost = config.getBoolean("autoHost", true);
        autoHostPort = config.getInt("autoHostPort", 8080);

        ssl = config.getBoolean("ssl", false);
        keystorePassword = config.getString("keystorePassword", "YOUR_PASSWORD");
        keystoreFileName = config.getString("keystoreFileName", "keystore.jks");

        webSocketPort = config.getInt("webSocketPort", 8081);
        webSocketHostName = config.getString("webSocketHostName", "localhost");

        maxVoiceDistance = (float) config.getDouble("maxVoiceDistance", 30);

        connectionMessage = config.getString("connectionMessage", "§6Join the audio client by clicking here!");
        connectionHoverMessage = config.getString("connectionHoverMessage", "Click here");

        distanceModel = config.getString("distanceModel", "exponential");
        refDistance = (float) config.getDouble("refDistance", 3);
        rolloffFactor = (float) config.getDouble("rolloffFactor", 2);
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public String getClientLink() {
        return clientLink;
    }

    public boolean isAutoHost() {
        return autoHost;
    }

    public int getAutoHostPort() {
        return autoHostPort;
    }

    public boolean isSsl() {
        return ssl;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public String getKeystoreFileName() {
        return keystoreFileName;
    }

    public int getWebSocketPort() {
        return webSocketPort;
    }

    public String getWebSocketHostName() {
        return webSocketHostName;
    }

    public float getMaxVoiceDistance() {
        return maxVoiceDistance;
    }

    public String getConnectionMessage() {
        return connectionMessage;
    }

    public String getConnectionHoverMessage() {
        return connectionHoverMessage;
    }

    public String getDistanceModel() {
        return distanceModel;
    }

    public float getRefDistance() {
        return refDistance;
    }

    public float getRolloffFactor() {
        return rolloffFactor;
    }

}