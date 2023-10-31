package fr.supermax_8.boostedaudio.core.pluginmessage;

import com.google.gson.annotations.Expose;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;

import java.util.List;

public class UsersFromUuids {

    @Expose
    List<HostUser> users;

    public UsersFromUuids(List<HostUser> users) {
        this.users = users;
    }

    public List<HostUser> getUsers() {
        return users;
    }

}