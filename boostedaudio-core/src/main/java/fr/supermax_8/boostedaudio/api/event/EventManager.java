package fr.supermax_8.boostedaudio.api.event;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventManager {

    private static final EventManager instance = new EventManager();
    private final ConcurrentHashMap<Class<? extends Event>, EnumMap<EventPriority, Listener>> listeners = new ConcurrentHashMap<>();


    protected EventManager() {
    }


    public <E extends Event> Listener registerListener(EventPriority priority, Class<E> eventClass, Consumer<E> consumer) {
        Listener listener = new Listener((Consumer<Event>) consumer, UUID.randomUUID());
        getListeners(eventClass).put(priority, listener);
        return listener;
    }

    public void unregisterListener(Listener listener) {
        listeners.forEach((aClass, eventPriorityListenerEnumMap) -> {
            Iterator<Map.Entry<EventPriority, Listener>> iterator = eventPriorityListenerEnumMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<EventPriority, Listener> entry = iterator.next();
                if (entry.getValue().getUuid().equals(listener.getUuid())) {
                    iterator.remove();
                    break;
                }
            }
        });
    }

    public void callEvent(Event event) {
        getListeners(event.getClass()).forEach((priority, listener) -> listener.getEventConsumer().accept(event));
    }

    private EnumMap<EventPriority, Listener> getListeners(Class<? extends Event> eventClass) {
        return listeners.computeIfAbsent(eventClass, aClass -> new EnumMap<>(EventPriority.class));
    }

    public static EventManager getInstance() {
        return instance;
    }

}