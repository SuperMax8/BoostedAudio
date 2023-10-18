package fr.supermax_8.boostedaudio.spigot.utils.configuration;

import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfigurationSection;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;

public class SpigotCrossConfigurationSection extends CrossConfigurationSection {
    private ConfigurationSection section;

    public SpigotCrossConfigurationSection(ConfigurationSection section) {
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
        return section.isConfigurationSection(path);
    }

    @Override
    public CrossConfigurationSection getConfigurationSection(String path) {
        return from(section.getConfigurationSection(path));
    }

    @Override
    public Collection<String> getKeys(boolean deep) {
        return section.getKeys(deep);
    }

    @Override
    public String getName() {
        return section.getName();
    }

}