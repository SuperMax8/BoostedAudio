package fr.supermax_8.boostedaudio.api.event.events;

import fr.supermax_8.boostedaudio.api.User;

public class VoiceChatUnlinkEvent extends VoiceChatLinkEvent {

    public VoiceChatUnlinkEvent(String layerId, User user1, User user2) {
        super(layerId, user1, user2);
    }

}