package fr.supermax_8.boostedaudio.spring;

import org.bukkit.plugin.Plugin;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

public class SpringSpigotInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private final Plugin plugin;

    public SpringSpigotInitializer(Plugin plugin) {
        this.plugin = plugin;
    }

    public void initialize(ConfigurableApplicationContext context) {
        MutablePropertySources propertySources = context.getEnvironment().getPropertySources();
        final Resource resource = new ClassPathResource("application.properties");
        try {
            propertySources.addFirst(new PropertiesPropertySource("application.properties", PropertiesLoaderUtils.loadProperties(resource)));
        } catch (IOException e) {
            e.printStackTrace();
        }


        Properties props = new Properties();
        props.put("spigot.plugin", plugin.getName());
        propertySources.addLast(new PropertiesPropertySource("spring-bukkit", props));
    }

}