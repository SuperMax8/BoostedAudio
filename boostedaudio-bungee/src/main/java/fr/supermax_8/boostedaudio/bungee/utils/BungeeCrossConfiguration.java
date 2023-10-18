package fr.supermax_8.boostedaudio.bungee.utils;

import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfiguration;
import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfigurationSection;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;

import java.io.File;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class BungeeCrossConfiguration extends CrossConfiguration {

    private Configuration config;
    private File configFile;

    public BungeeCrossConfiguration() {
        this.configFile = null;
        try {
            config = net.md_5.bungee.config.YamlConfiguration.getProvider(net.md_5.bungee.config.YamlConfiguration.class).load("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BungeeCrossConfiguration(Object o) {
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
        return new ArrayList<>(config.getKeys());
    }

    @Override
    public boolean contains(String path) {
        return config.contains(path);
    }

    @Override
    public boolean isConfigurationSectionObject(Object o) {
        return o instanceof Configuration;
    }

    @Override
    public CrossConfigurationSection getConfigurationSection(String path) {
        return CrossConfigurationSection.from(config.getSection(path));
    }

    @Override
    public boolean isConfigurationSection(String path) {
        return config.getSection(path) != null;
    }

    @Override
    public String saveToString() {
        StringWriter sw = new StringWriter();
        ConfigurationProvider.getProvider(net.md_5.bungee.config.YamlConfiguration.class).save(config, sw);
        return sw.toString();
    }

    @Override
    public CrossConfiguration load(Object o) {
        if (o instanceof Reader) {
            try {
                config = net.md_5.bungee.config.YamlConfiguration.getProvider(net.md_5.bungee.config.YamlConfiguration.class).load(configFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.configFile = null;
        } else if (o instanceof File file) {
            try {
                config = net.md_5.bungee.config.YamlConfiguration.getProvider(net.md_5.bungee.config.YamlConfiguration.class).load(configFile);
                configFile = file;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else throw new RuntimeException("Cannot load data");
        return this;
    }

    @Override
    public void save() {
        save(configFile);
    }

    @Override
    public void save(File f) {
        try {
            ConfigurationProvider.getProvider(net.md_5.bungee.config.YamlConfiguration.class).save(config, f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}