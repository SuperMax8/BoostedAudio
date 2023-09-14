package fr.supermax_8.boostedaudio.spring;

import fr.supermax_8.boostedaudio.BoostedAudioLoader;
import org.springframework.context.ConfigurableApplicationContext;

public class Loader {

    private static ConfigurableApplicationContext defaultLoadcontext;

    public Loader() {
        try {
            defaultLoadcontext = SpringSpigotBootstrapper.initialize(BoostedAudioLoader.getInstance(), this.getClass().getClassLoader(), SpringLoader.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        defaultLoadcontext.close();
    }



}