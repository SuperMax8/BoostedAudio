package fr.supermax_8.boostedaudio.bungee.utils;

import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfigurationSection;
import net.md_5.bungee.config.Configuration;

import java.util.Collection;

public class BungeeCrossConfigurationSection extends CrossConfigurationSection {
    private final Configuration section;

    public BungeeCrossConfigurationSection(Configuration section) {
        this.section = section;
    }

    @Override
    public Object get(String path) {
        return section.get(path);
    }

    @Override
    public Object get(String path, Object defaultValue) {
        return section.get(path, defaultValue);
    }

    @Override
    public void set(String path, Object value) {
        section.set(path, value);
    }

    @Override
    public boolean contains(String path) {
        return section.contains(path);
    }

    @Override
    public boolean isConfigurationSection(String path) {
        return section.getSection(path) != null;
    }

    @Override
    public Collection<String> getKeys(boolean deep) {
        return section.getKeys();
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public CrossConfigurationSection getConfigurationSection(String path) {
        return from(section.getSection(path));
    }

}