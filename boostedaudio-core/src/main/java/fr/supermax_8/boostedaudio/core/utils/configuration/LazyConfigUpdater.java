package fr.supermax_8.boostedaudio.core.utils.configuration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class LazyConfigUpdater {


    public static void update(CrossConfiguration toUpdate, InputStream configFileStream, File configFile) {
        CrossConfiguration resourceConfig = CrossConfiguration.newConfig().load(new InputStreamReader(configFileStream));

        List<String> toUpdateKeys = toUpdate.getKeys(true);
        List<String> ressourceConfigKeys = resourceConfig.getKeys(true);

        // Check if no new keys were added
        if (toUpdateKeys.equals(ressourceConfigKeys)) return;

        // Resource is up to date, so we just put old values into the new config
        for (String key : toUpdateKeys) resourceConfig.set(key, toUpdate.get(key));
        resourceConfig.save(configFile);
    }

}