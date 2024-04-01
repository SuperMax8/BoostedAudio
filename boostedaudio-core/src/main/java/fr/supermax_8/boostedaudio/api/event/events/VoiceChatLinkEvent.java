package fr.supermax_8.boostedaudio.api.event.events;

import fr.supermax_8.boostedaudio.api.event.Event;
import fr.supermax_8.boostedaudio.api.User;

public class VoiceChatLinkEvent extends Event {

    private final String layerId;
    private final User user1;
    private final User user2;

    public VoiceChatLinkEvent(String layerId, User user1, User user2) {
        this.layerId = layerId;

        this.user1 = user1;
        this.user2 = user2;
    }

    public String getLayerId() {
        return layerId;
    }

    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }

}