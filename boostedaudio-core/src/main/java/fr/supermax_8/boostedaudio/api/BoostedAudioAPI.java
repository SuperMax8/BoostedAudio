package fr.supermax_8.boostedaudio.api;

import com.google.gson.Gson;
import fr.supermax_8.boostedaudio.api.event.EventManager;
import fr.supermax_8.boostedaudio.core.BoostedAudioAPIImpl;
import fr.supermax_8.boostedaudio.core.BoostedAudioConfiguration;
import fr.supermax_8.boostedaudio.core.InternalAPI;
import fr.supermax_8.boostedaudio.core.proximitychat.VoiceLayer;
import org.wildfly.common.annotation.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface BoostedAudioAPI {


    BoostedAudioAPI api = BoostedAudioAPIImpl.getApi();

    /**
     * @return An object to communicate with the host, it can be the current server but can also be the bungee so execute that in async
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

    void addVoiceLayer(String uniqueId, boolean audioSpatialized, int priority, @Nullable Predicate<UUID> playerInLayer);

    void removeVoiceLayer(String uniqueId);

    static BoostedAudioAPI getAPI() {
        return api;
    }

}