package fr.supermax_8.boostedaudio.spigot;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class HostRequester {

    private final ConcurrentHashMap<String, List<Consumer>> waitingRequests = new ConcurrentHashMap<>();

    public <R> void request(String channel, String input, Consumer<R> whenReceived, Class<R> requestedClass) {
        if (!waitingRequests.containsKey(channel)) {
            BoostedAudioSpigot.registerOutgoingPluginMessage(channel);
            BoostedAudioSpigot.registerIncomingPluginMessage(channel, (s, player, bytes) -> {
                System.out.println("Receiving something from " + channel);
                try {
                    waitingRequests.get(channel).remove(0);
                    R r = BoostedAudioAPI.api.getGson().fromJson(new String(bytes), requestedClass);
                    whenReceived.accept(r);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        }
        waitingRequests.computeIfAbsent(channel, k -> new LinkedList<>()).add(whenReceived);
        BoostedAudioSpigot.sendPluginMessage(channel, input);
        System.out.println("Sending something to " + channel);
    }


}