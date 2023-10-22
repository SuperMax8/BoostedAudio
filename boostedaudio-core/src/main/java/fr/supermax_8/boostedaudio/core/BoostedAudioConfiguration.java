package fr.supermax_8.boostedaudio.core;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfiguration;
import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfigurationSection;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

public class BoostedAudioConfiguration {

    private String data = "%%__USER__%% %%__RESOURCE__%% %%__NONCE__%%";

    private File dataFolder;

    private boolean debugMode;
    private boolean bungeecoord;
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
    private boolean sendOnConnect;
    private int sendOnConnectDelay;
    private boolean customClient;
    private String connectionMessage;
    private String connectionHoverMessage;
    private List<String> clientConfig;
    private String clientWebSocketLink;
    private boolean notification;

    public BoostedAudioConfiguration(File configFile) {
        load(configFile);
        BoostedAudioAPI.api.info("Configuratuion loaded");
    }

    private void load(File configFile) {
        dataFolder = configFile.getParentFile();

        CrossConfiguration config = CrossConfiguration.newConfig().load(configFile);

        notification = (boolean) config.get("notification", true);
        debugMode = (boolean) config.get("debugMode");

        bungeecoord = (boolean) config.get("bungeecoord", false);

        clientLink = (String) config.get("client-link", "http://localhost:8080");
        clientWebSocketLink = (String) config.get("clientWebSocketLink", "ws://localhost:8081");
        autoHost = (boolean) config.get("autoHost", true);
        autoHostPort = (int) config.get("autoHostPort", 8080);

        CrossConfigurationSection sslSection = config.getConfigurationSection("ssl");
        if (sslSection != null) {
            ssl = (boolean) sslSection.get("ssl", false);
            keystorePassword = (String) sslSection.get("keystorePassword", "YOUR_PASSWORD");
            keystoreFileName = (String) sslSection.get("keystoreFileName", "keystore.jks");
        }

        CrossConfigurationSection webSocketSection = config.getConfigurationSection("webSocket");
        if (webSocketSection != null) {
            webSocketPort = (int) webSocketSection.get("webSocketPort", 8081);
            webSocketHostName = (String) webSocketSection.get("webSocketHostName", "localhost");
        }

        CrossConfigurationSection voiceChatSection = config.getConfigurationSection("voicechat");
        if (voiceChatSection != null) {
            voiceChatEnabled = (boolean) voiceChatSection.get("voicechat", true);
            maxVoiceDistance = ((Number) voiceChatSection.get("maxVoiceDistance", 30)).floatValue();
            distanceModel = (String) voiceChatSection.get("distanceModel", "exponential");
            refDistance = ((Number) voiceChatSection.get("refDistance", 4)).floatValue();
            rolloffFactor = ((Number) voiceChatSection.get("rolloffFactor", 1)).floatValue();
        }

        sendOnConnect = (boolean) config.get("sendOnConnect", true);
        sendOnConnectDelay = (int) config.get("sendOnConnectDelay", 30);

        customClient = (boolean) config.get("customClient", false);

        connectionMessage = (String) config.get("connectionMessage", "§6Join the audio client by clicking here!");
        connectionHoverMessage = (String) config.get("connectionHoverMessage", "Click here");

        clientConfig = (List<String>) config.get("clientConfig");
        if (isDebugMode()) showConfiguration();
    }

    public void showConfiguration() {
        Class<?> classs = this.getClass();
        Field[] fields = classs.getDeclaredFields();

        System.out.println("Instance of class " + classs.getName());
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                System.out.println(field.getName() + ": " + value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> getClientConfig() {
        return clientConfig;
    }

    public boolean isDebugMode() {
        if (debugMode) System.out.println("DebugMode: ");
        return debugMode;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public boolean isBungeecoord() {
        return bungeecoord;
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

    public boolean isNotification() {
        return notification;
    }

    public String getClientWebSocketLink() {
        return clientWebSocketLink;
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

    public boolean isSendOnConnect() {
        return sendOnConnect;
    }

    public int getSendOnConnectDelay() {
        return sendOnConnectDelay;
    }

    public boolean isCustomClient() {
        return customClient;
    }

    public String getConnectionMessage() {
        return connectionMessage;
    }

    public String getConnectionHoverMessage() {
        return connectionHoverMessage;
    }

}