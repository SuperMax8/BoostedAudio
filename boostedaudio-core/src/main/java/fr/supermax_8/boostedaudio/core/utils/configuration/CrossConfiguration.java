package fr.supermax_8.boostedaudio.core.utils.configuration;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;

/*1.1*/
public abstract class CrossConfiguration {

    // You need to write this function with the corresponding server soft you use
    public static Supplier<CrossConfiguration> instancer;

    public CrossConfiguration() {

    }

    public abstract void set(String s, Object o);

    public abstract Object get(String s);

    public abstract Object get(String s, Object defaultValue);

    public abstract String getString(String s);

    public abstract List<String> getKeys(boolean deep);

    public abstract boolean contains(String path);

    public abstract boolean isConfigurationSectionObject(Object o);

    public abstract CrossConfigurationSection getConfigurationSection(String path);

    public abstract boolean isConfigurationSection(String path);

    public abstract String saveToString();

    public abstract CrossConfiguration load(Object o);

    public abstract void save();

    public abstract void save(File f);

    public static CrossConfiguration newConfig() {
        return instancer.get();
    }

}