package fr.supermax_8.boostedaudio.api.event.events;

import fr.supermax_8.boostedaudio.api.user.User;

public class UserJoinEvent extends UserEvent {

    public UserJoinEvent(User user) {
        super(user);
    }

}