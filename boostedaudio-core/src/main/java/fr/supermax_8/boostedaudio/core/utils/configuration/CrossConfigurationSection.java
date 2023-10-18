package fr.supermax_8.boostedaudio.core.utils.configuration;

import java.util.Collection;
import java.util.function.Function;

// A wrapper for the Bukkit and Bungee configuration classes
/*1.0*/
public abstract class CrossConfigurationSection {

    public static Function<Object, CrossConfigurationSection> converter;

    public abstract Object get(String path);
    public abstract Object get(String path, Object defaultValue);

    public abstract void set(String path, Object value);

    public abstract boolean contains(String path);

    public abstract boolean isConfigurationSection(String path);
    public abstract CrossConfigurationSection getConfigurationSection(String path);
    public abstract Collection<String> getKeys(boolean deep);

    public abstract String getName();

    public static CrossConfigurationSection from(Object section) {
        return converter.apply(section);
    }


}