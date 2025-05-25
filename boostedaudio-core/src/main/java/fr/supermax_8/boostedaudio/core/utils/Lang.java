package fr.supermax_8.boostedaudio.core.utils;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Lang {

    private static ConcurrentHashMap<String, String> lang = new ConcurrentHashMap<>();


    public static String get(String id, Object... obj) {
        String text = lang.get(id);
        if (text == null) return "LANG ISSUE, MAYBE CALL THE DEV THERE IS A PROBLEM";
        if (obj.length == 0) return text;

        StringBuilder textBuilder = new StringBuilder(lang.get(id));
        int count = 0;
        for (Object o : obj) {
            if (o != null) replaceAll(textBuilder, "{" + count + "}", o.toString());
            count++;
        }
        return textBuilder.toString();
    }

    private static void replaceAll(StringBuilder builder, String toReplace, String replacement) {
        int toReplaceLength = toReplace.length();
        int indexToReplace = builder.indexOf(toReplace);
        while (indexToReplace != -1) {
            builder.replace(indexToReplace, indexToReplace + toReplaceLength, replacement);
            indexToReplace = builder.indexOf(toReplace);
        }
    }

    public static void init(File mainDir) {
        try {
            File[] langFiles = mainDir.listFiles((f, name) -> name.contains("lang"));
            File langFile;
            if (langFiles.length == 0) {
                langFile = new File(mainDir, "language.yml");
            } else langFile = langFiles[0];

            YamlDocument config = YamlDocument.create(
                    langFile,
                    ResourceUtils.getResourceAsStream("language.yml"),
                    GeneralSettings.builder().build(),
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.builder().build(),
                    UpdaterSettings.builder().build()
            );
            Set<Route> routes = config.getRoutes(false);
            int count = 0;
            for (Route route : routes) {
                try {
                    lang.put(route.join('.'), config.getString(route));
                } catch (Exception e) {
                    System.out.println("YAML Error in lang file line: " + count + ", Error: " + e);
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}