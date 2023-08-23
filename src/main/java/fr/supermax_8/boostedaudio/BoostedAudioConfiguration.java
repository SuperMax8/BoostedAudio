package fr.supermax_8.boostedaudio;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;

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
    private boolean voiceChatEnabled;
    private float maxVoiceDistance;
    private String distanceModel;
    private float refDistance;
    private float rolloffFactor;
    private String connectionMessage;
    private String connectionHoverMessage;

    public BoostedAudioConfiguration() {
        load();
    }

    private void load() {
        BoostedAudio.getInstance().saveDefaultConfig();
        FileConfiguration config = BoostedAudio.getInstance().getConfig();

        debugMode = config.getBoolean("debugMode");

        clientLink = config.getString("client-link", "http://localhost:8080");
        autoHost = config.getBoolean("autoHost", true);
        autoHostPort = config.getInt("autoHostPort", 8080);

        ConfigurationSection sslSection = config.getConfigurationSection("ssl");
        if (sslSection != null) {
            ssl = sslSection.getBoolean("ssl", false);
            keystorePassword = sslSection.getString("keystorePassword", "YOUR_PASSWORD");
            keystoreFileName = sslSection.getString("keystoreFileName", "keystore.jks");
        }

        ConfigurationSection webSocketSection = config.getConfigurationSection("webSocket");
        if (webSocketSection != null) {
            webSocketPort = webSocketSection.getInt("webSocketPort", 8081);
            webSocketHostName = webSocketSection.getString("webSocketHostName", "localhost");
        }

        ConfigurationSection voiceChatSection = config.getConfigurationSection("voicechat");
        if (voiceChatSection != null) {
            voiceChatEnabled = voiceChatSection.getBoolean("voicechat", true);
            maxVoiceDistance = (float) voiceChatSection.getDouble("maxVoiceDistance", 30);
            distanceModel = voiceChatSection.getString("distanceModel", "exponential");
            refDistance = (float) voiceChatSection.getDouble("refDistance", 3);
            rolloffFactor = (float) voiceChatSection.getDouble("rolloffFactor", 2);
        }

        connectionMessage = config.getString("connectionMessage", "§6Join the audio client by clicking here!");
        connectionHoverMessage = config.getString("connectionHoverMessage", "Click here");
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

    public boolean isVoiceChatEnabled() {
        return voiceChatEnabled;
    }

    public float getMaxVoiceDistance() {
        return maxVoiceDistance;
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

    public String getConnectionMessage() {
        return connectionMessage;
    }

    public String getConnectionHoverMessage() {
        return connectionHoverMessage;
    }
}
