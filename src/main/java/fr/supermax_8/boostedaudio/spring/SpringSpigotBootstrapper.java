package fr.supermax_8.boostedaudio.spring;

import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SpringSpigotBootstrapper {


    public static ConfigurableApplicationContext initialize(JavaPlugin plugin,
                                                            Class<?> applicationClass) throws ExecutionException, InterruptedException {
        URLClassLoader classLoader = createCombinedClassLoader(plugin.getClass().getClassLoader(),
                Thread.currentThread().getContextClassLoader());
        return initialize(plugin, classLoader, new SpringApplicationBuilder(applicationClass));
    }

    public static ConfigurableApplicationContext initialize(JavaPlugin plugin,
                                                            SpringApplicationBuilder builder) throws ExecutionException, InterruptedException {
        URLClassLoader classLoader = createCombinedClassLoader(plugin.getClass().getClassLoader(),
                Thread.currentThread().getContextClassLoader());
        return initialize(plugin, classLoader, builder);
    }

    public static ConfigurableApplicationContext initialize(JavaPlugin plugin,
                                                            ClassLoader classLoader, Class<?> applicationClass) throws ExecutionException, InterruptedException {
        return initialize(plugin, classLoader, new SpringApplicationBuilder(applicationClass));
    }

    public static ConfigurableApplicationContext initialize(JavaPlugin plugin,
                                                            ClassLoader classLoader, SpringApplicationBuilder builder) throws ExecutionException, InterruptedException {

        if (!isPaperServer()) {
            System.out.println("using spigot server executor");
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                // ConfigurableApplicationContext c = initializeByExecutor(plugin, classLoader,
                // builder);
                Future<ConfigurableApplicationContext> contextFuture = executor
                        .submit(() -> initializeByExecutor(plugin, classLoader, builder));
                return contextFuture.get();
            } finally {
                executor.shutdown();
            }
        }

        System.out.println("using paper server executor");
        return initializeByExecutor(plugin, classLoader, builder);

    }

    private static ConfigurableApplicationContext initializeByExecutor(JavaPlugin plugin, ClassLoader classLoader,
                                                                       SpringApplicationBuilder builder) {

        Thread.currentThread().setContextClassLoader(classLoader);

        Properties props = new Properties();
        try {
            props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties"));
        } catch (Exception ignored) {
        }

        if (builder.application().getResourceLoader() == null) {
            DefaultResourceLoader loader = new DefaultResourceLoader(classLoader);
            builder.resourceLoader(loader);
        }
        return builder
                .properties(props)
                .initializers(new SpringSpigotInitializer(plugin))
                .run();
    }

    private static URLClassLoader createCombinedClassLoader(ClassLoader parentClassLoader,
                                                            ClassLoader contextClassLoader) {
        URLClassLoader pluginClassLoader = (URLClassLoader) parentClassLoader;
        URLClassLoader contextClassLoaderURL = (URLClassLoader) contextClassLoader;

        URL[] pluginClassLoaderUrls = pluginClassLoader.getURLs();
        URL[] contextClassLoaderUrls = contextClassLoaderURL.getURLs();

        URL[] combinedUrls = new URL[pluginClassLoaderUrls.length + contextClassLoaderUrls.length];
        System.arraycopy(pluginClassLoaderUrls, 0, combinedUrls, 0, pluginClassLoaderUrls.length);
        System.arraycopy(contextClassLoaderUrls, 0, combinedUrls, pluginClassLoaderUrls.length,
                contextClassLoaderUrls.length);

        return new URLClassLoader(combinedUrls, parentClassLoader);
    }

    public static boolean isPaperServer() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig"); // Check for Paper class
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}