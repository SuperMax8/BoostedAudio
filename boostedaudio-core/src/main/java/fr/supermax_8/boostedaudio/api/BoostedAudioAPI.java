package fr.supermax_8.boostedaudio.api;

import com.google.gson.Gson;
import fr.supermax_8.boostedaudio.api.event.EventManager;
import fr.supermax_8.boostedaudio.core.BoostedAudioAPIImpl;
import fr.supermax_8.boostedaudio.core.BoostedAudioConfiguration;
import fr.supermax_8.boostedaudio.core.InternalAPI;

import java.util.List;

public interface BoostedAudioAPI {


    BoostedAudioAPI api = BoostedAudioAPIImpl.getApi();

    /**
     * @return An object to communicate with the host, it can be current server but can also be the bungee so execute that in async
     */
    HostProvider getHostProvider();

    /**
     * @return The gson obj use in the plugin
     */
    Gson getGson();

    BoostedAudioConfiguration getConfiguration();

    InternalAPI getInternalAPI();

    List<String> getMultiServerSecrets();

    EventManager getEventManager();

    void info(String message);

    void debug(String message);

    static BoostedAudioAPI getAPI() {
        return api;
    }


}