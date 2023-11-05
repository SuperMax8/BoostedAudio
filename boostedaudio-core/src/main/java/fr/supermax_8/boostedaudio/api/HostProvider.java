package fr.supermax_8.boostedaudio.api;

import fr.supermax_8.boostedaudio.api.user.User;

import java.util.Map;
import java.util.UUID;

public interface HostProvider {


    /**
     * @return a map of all players on the server that is calling this method connected to the audio panel
     */
    Map<UUID, User> getUsersOnServer();

    void waitUntilPluginSetup();

}