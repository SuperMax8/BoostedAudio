package fr.supermax_8.boostedaudio.spigot.utils.configuration;

import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfiguration;
import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfigurationSection;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class SpigotCrossConfiguration extends CrossConfiguration {

    private FileConfiguration config;
    private File configFile;

    public SpigotCrossConfiguration() {
        config = new YamlConfiguration();
        configFile = null;
    }

    private SpigotCrossConfiguration(Object o) {
        load(o);
    }

    @Override
    public void set(String s, Object o) {
        config.set(s, o);
    }

    @Override
    public Object get(String s) {
        return config.get(s);
    }

    @Override
    public Object get(String s, Object defaultValue) {
        return config.get(s, defaultValue);
    }

    @Override
    public String getString(String s) {
        return config.getString(s);
    }

    @Override
    public List<String> getKeys(boolean deep) {
        return new ArrayList<>(config.getKeys(deep));
    }

    @Override
    public boolean contains(String path) {
        return config.contains(path);
    }

    @Override
    public boolean isConfigurationSectionObject(Object o) {
        return o instanceof ConfigurationSection;
    }

    @Override
    public CrossConfigurationSection getConfigurationSection(String path) {
        return CrossConfigurationSection.from(config.getConfigurationSection(path));
    }

    @Override
    public boolean isConfigurationSection(String path) {
        return config.isConfigurationSection(path);
    }

    @Override
    public String saveToString() {
        return config.saveToString();
    }

    @Override
    public CrossConfiguration load(Object o) {
        if (o instanceof Reader reader) {
            try {
                config = YamlConfiguration.loadConfiguration(reader);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.configFile = null;
        } else if (o instanceof File file) {
            try {
                config = YamlConfiguration.loadConfiguration(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            configFile = file;
        } else throw new RuntimeException("Cannot load the data !");
        return this;
    }

    @Override
    public void save() {
        save(configFile);
    }

    @Override
    public void save(File f) {
        try {
            config.save(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}