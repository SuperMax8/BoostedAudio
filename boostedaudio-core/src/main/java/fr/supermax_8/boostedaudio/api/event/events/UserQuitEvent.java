package fr.supermax_8.boostedaudio.api.event.events;

import fr.supermax_8.boostedaudio.api.user.User;

public class UserQuitEvent extends UserEvent {


    public UserQuitEvent(User user) {
        super(user);
    }

}