package fr.supermax_8.boostedaudio.api.event;

import java.util.UUID;
import java.util.function.Consumer;

public class Listener {

    private final Consumer<Event> eventConsumer;
    private final UUID uuid;

    public Listener(Consumer<Event> eventConsumer, UUID uuid) {
        this.eventConsumer = eventConsumer;
        this.uuid = uuid;
    }

    public Consumer<Event> getEventConsumer() {
        return eventConsumer;
    }

    public UUID getUuid() {
        return uuid;
    }

}