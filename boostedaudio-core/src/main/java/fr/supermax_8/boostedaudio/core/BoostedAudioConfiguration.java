package fr.supermax_8.boostedaudio.core;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.utils.Base64Utils;
import fr.supermax_8.boostedaudio.core.utils.ResourceUtils;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class BoostedAudioConfiguration {

    private static String data = "%%__USER__%% %%__RESOURCE__%% %%__NONCE__%%";

    private File dataFolder;

    private boolean debugMode;
    private boolean diffuser;
    private List<String> secrets;
    private String mainProxyWebsocketLink;
    private String clientLink;
    private boolean autoHost;
    private int autoHostPort;
    private boolean ssl;
    private List<String> iceServers;
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
    private List<String> connectionMessage;
    private String connectionHoverMessage;
    private List<String> clientConfig;
    private String clientWebSocketLink;
    private boolean notification;

    private boolean sendQRcodeOnConnect;
    private String qrCodeTitle;
    private String connectedSymbol;
    private String mutedSymbol;
    private String notconnectedSymbol;
    private String proxyServerName;

    public BoostedAudioConfiguration(File configFile) {
        try {
            load(configFile);
            BoostedAudioAPI.api.info("Configuratuion loaded");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load(File configFile) throws IOException {
        dataFolder = configFile.getParentFile();
        try {
            // ConfigUpdater.update(ResourceUtils::getResourceAsStream, "config.yml", configFile);
            //LazyConfigUpdater.update(CrossConfiguration.newConfig().load(configFile), ResourceUtils.getResourceAsStream("config.yml"), configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        YamlDocument config = YamlDocument.create(
                configFile,
                ResourceUtils.getResourceAsStream("config.yml"),
                GeneralSettings.builder().build(),
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.builder().build(),
                UpdaterSettings.builder().build()
        );

        notification = (boolean) config.get("notification", true);
        debugMode = (boolean) config.get("debugMode");

        diffuser = config.getBoolean("diffuser", false);
        mainProxyWebsocketLink = (String) config.get("mainProxyWebsocketLink");
        secrets = config.getStringList("secrets");
        proxyServerName = config.getString("proxyServerName");

        clientLink = (String) config.get("client-link");
        clientWebSocketLink = (String) config.get("clientWebSocketLink");
        autoHost = (boolean) config.get("autoHost", true);
        autoHostPort = (int) config.get("autoHostPort", 8080);

        ssl = (boolean) config.get("ssl.ssl", false);
        iceServers = config.getStringList("iceServers");

        keystorePassword = (String) config.get("ssl.keystorePassword", "YOUR_PASSWORD");
        keystoreFileName = (String) config.get("ssl.keystoreFileName", "keystore.jks");

        webSocketPort = (int) config.get("webSocket.webSocketPort", 8081);
        webSocketHostName = (String) config.get("webSocket.webSocketHostName", "localhost");

        voiceChatEnabled = (boolean) config.get("voicechat.voicechat", true);
        maxVoiceDistance = ((Number) config.get("voicechat.maxVoiceDistance", 30)).floatValue();
        distanceModel = (String) config.get("voicechat.distanceModel", "exponential");
        refDistance = ((Number) config.get("voicechat.refDistance", 4)).floatValue();
        rolloffFactor = ((Number) config.get("voicechat.rolloffFactor", 1)).floatValue();

        sendOnConnect = (boolean) config.get("sendOnConnect", true);
        sendOnConnectDelay = (int) config.get("sendOnConnectDelay", 30);

        sendQRcodeOnConnect = (boolean) config.get("sendQRcodeOnConnect", true);
        qrCodeTitle = (String) config.get("qrCodeTitle", "qrCode");

        customClient = (boolean) config.get("customClient", false);

        Object connectionMessageObj = config.get("connectionMessage");
        connectionMessage = connectionMessageObj instanceof String ? Collections.singletonList((String) connectionMessageObj) : ((List<String>) connectionMessageObj);
        connectionHoverMessage = (String) config.get("connectionHoverMessage", "Click here");

        connectedSymbol = config.getString("connectedSymbol");
        mutedSymbol = config.getString("mutedSymbol");
        notconnectedSymbol = config.getString("notconnectedSymbol");

        clientConfig = config.getStringList("clientConfig");

        Map<String, String> defaultParams = convertConfigList(YamlDocument.create(ResourceUtils.getResourceAsStream("config.yml"))
                .getStringList("clientConfig"));
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

        boolean modified = false;

        if (updated) {
            config.set("clientConfig", clientConfig);
            modified = true;
            BoostedAudioAPI.api.info("The clientConfig has been updated, new parameters have been added !");
        }

        if (secrets.isEmpty() || secrets.get(0).isEmpty()) {
            secrets.clear();
            secrets.add(Base64Utils.generateSecuredToken(16));
            config.set("secrets", secrets);
            modified = true;
        }

        if (modified) config.save();

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