package fr.supermax_8.boostedaudio.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringLoader {

    public SpringLoader() {
        startSpringApplication();
    }

    private static ConfigurableApplicationContext context;

    public static void startSpringApplication() {
        context = SpringApplication.run(Spring.class);
    }

    public static void stopSpringApplication() {

        context.close();
        context = null;

    }

}