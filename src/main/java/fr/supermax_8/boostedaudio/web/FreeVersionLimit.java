package fr.supermax_8.boostedaudio.web;

import fr.supermax_8.boostedaudio.BoostedAudio;

public class FreeVersionLimit {


    public static int getMaxUserConnected() {
        return BoostedAudio.getInstance().getConfiguration().isVoiceChatEnabled() ? 10 : 8;
    }

}