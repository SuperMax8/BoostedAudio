package fr.supermax_8.boostedaudio.core;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.utils.Base64Utils;
import fr.supermax_8.boostedaudio.core.utils.ResourceUtils;
import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfiguration;
import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfigurationSection;
import fr.supermax_8.boostedaudio.core.utils.configuration.LazyConfigUpdater;
import lombok.Getter;

import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class BoostedAudioConfiguration {

    private static String data = "%%__USER__%% %%__RESOURCE__%% %%__NONCE__%%";

    private File dataFolder;

    private boolean debugMode;
    private boolean bungeecoord;
    private List<String> bungeeSecrets;
    private String bungeeWebsocketLink;
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

    private boolean sendQRcodeOnConnect;
    private String qrCodeTitle;
    private String connectedSymbol;
    private String mutedSymbol;
    private String notconnectedSymbol;

    public BoostedAudioConfiguration(File configFile) {
        load(configFile);
        BoostedAudioAPI.api.info("Configuratuion loaded");
    }

    private void load(File configFile) {
        dataFolder = configFile.getParentFile();
        try {
            // ConfigUpdater.update(ResourceUtils::getResourceAsStream, "config.yml", configFile);
            LazyConfigUpdater.update(CrossConfiguration.newConfig().load(configFile), ResourceUtils.getResourceAsStream("config.yml"), configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        CrossConfiguration config = CrossConfiguration.newConfig().load(configFile);

        notification = (boolean) config.get("notification", true);
        debugMode = (boolean) config.get("debugMode");

        bungeecoord = (boolean) config.get("bungeecoord", false);
        bungeeWebsocketLink = (String) config.get("bungeeWebsocketLink", "wss://localhost:8081");
        bungeeSecrets = (List<String>) config.get("bungeeSecrets");

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

        sendQRcodeOnConnect = (boolean) config.get("sendQRcodeOnConnect", true);
        qrCodeTitle = (String) config.get("qrCodeTitle", "qrCode");

        customClient = (boolean) config.get("customClient", false);

        connectionMessage = (String) config.get("connectionMessage", "§6Join the audio client by clicking here!");
        connectionHoverMessage = (String) config.get("connectionHoverMessage", "Click here");

        clientConfig = (List<String>) config.get("clientConfig");

        Map<String, String> defaultParams = convertConfigList((List<String>) CrossConfiguration.newConfig()
                .load(new InputStreamReader(ResourceUtils.getResourceAsStream("config.yml")))
                .get("clientConfig"));
        Map<String, String> params = convertConfigList(clientConfig);

        boolean updated = false;
        for (Map.Entry<String, String> param : defaultParams.entrySet()) {
            String key = param.getKey();
            String value = param.getValue();
            if (!params.containsKey(key)) {
                clientConfig.add(key + "=" + value);
                updated = true;
            }
        }
        if (updated) {
            config.set("clientConfig", clientConfig);
            config.save(configFile);
            BoostedAudioAPI.api.info("The clientConfig has been updated, new parameters have been added !");
        }

        if (bungeeSecrets.isEmpty() || bungeeSecrets.get(0).isEmpty()) {
            bungeeSecrets.clear();
            bungeeSecrets.add(Base64Utils.generateSecuredToken(16));
            config.set("bungeeSecrets", bungeeSecrets);
            config.save(configFile);
        }

        connectedSymbol = config.getString("connectedSymbol");
        mutedSymbol = config.getString("mutedSymbol");
        notconnectedSymbol = config.getString("notconnectedSymbol");

        if (isDebugMode()) showConfiguration();
    }

    public Map<String, String> convertConfigList(List<String> config) {
        HashMap<String, String> map = new HashMap<>();
        for (String s : config) {
            String[] placeholderEntry = s.split("=", 2);
            map.put(placeholderEntry[0], placeholderEntry[1]);
        }
        return map;
    }

    public void showConfiguration() {
        Class<?> classs = this.getClass();
        Field[] fields = classs.getDeclaredFields();

        BoostedAudioAPI.getAPI().info("Instance of class " + classs.getName());
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                BoostedAudioAPI.getAPI().info(field.getName() + ": " + value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


}