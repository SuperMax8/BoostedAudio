package fr.supermax_8.boostedaudio.core;

import dev.dejvokep.boostedyaml.YamlDocument;
import fr.supermax_8.boostedaudio.core.utils.ResourceUtils;

public class BaPluginVersion {

    private static String version;


    public static String getVersion() {
        if (version != null) return version;

        try {
            YamlDocument yamlDocument = YamlDocument.create(ResourceUtils.getResourceAsStream("plugin.yml"));
            version = yamlDocument.getString("version");
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "contact dev with full logs";
        }
    }


}