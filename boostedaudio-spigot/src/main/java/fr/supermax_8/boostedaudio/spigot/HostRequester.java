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
            BoostedAudioSpigot.registerServerPacketListener(channel, (message, serverId) -> {
                try {
                    List<Consumer> consumers = waitingRequests.get(channel);
                    if (consumers.isEmpty()) return;
                    Consumer consumer = consumers.remove(0);
                    R r = BoostedAudioAPI.api.getGson().fromJson(message, requestedClass);
                    consumer.accept(r);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        }
        waitingRequests.computeIfAbsent(channel, k -> new LinkedList<>()).add(whenReceived);
        BoostedAudioSpigot.sendServerPacket(channel, input);
    }


}