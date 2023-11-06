package fr.supermax_8.boostedaudio.api.event.events;

import fr.supermax_8.boostedaudio.api.event.Event;
import fr.supermax_8.boostedaudio.api.user.User;

public abstract class UserEvent extends Event {

    private final User user;

    public UserEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

}