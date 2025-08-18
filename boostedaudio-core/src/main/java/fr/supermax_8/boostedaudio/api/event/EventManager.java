package fr.supermax_8.boostedaudio.api.event;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventManager {

    @Getter
    private static final EventManager instance = new EventManager();
    // Map<EventClass, Map<Priority, List<Listener>>>
    private final ConcurrentHashMap<Class<? extends Event>, EnumMap<EventPriority, CopyOnWriteArrayList<Listener>>> listeners = new ConcurrentHashMap<>();

    protected EventManager() {
    }

    public <E extends Event> Listener registerListener(EventPriority priority, Class<E> eventClass, Consumer<E> consumer) {
        Listener listener = new Listener((Consumer<Event>) consumer, UUID.randomUUID());
        getListeners(eventClass)
                .computeIfAbsent(priority, p -> new CopyOnWriteArrayList<>()) // Thread-safe list
                .add(listener);
        return listener;
    }

    public void unregisterListener(Listener listener) {
        listeners.forEach((aClass, priorityMap) -> {
            priorityMap.forEach((priority, list) -> list.removeIf(l -> l.getUuid().equals(listener.getUuid())));
        });
    }

    public void callEvent(Event event) {
        getListeners(event.getClass())
                .forEach((priority, list) -> {
                    for (Listener listener : list) {
                        listener.getEventConsumer().accept(event);
                    }
                });
    }

    private EnumMap<EventPriority, CopyOnWriteArrayList<Listener>> getListeners(Class<? extends Event> eventClass) {
        return listeners.computeIfAbsent(eventClass, aClass -> new EnumMap<>(EventPriority.class));
    }
}