package fr.supermax_8.boostedaudio.utils;

import java.util.HashMap;
import java.util.Map;

public class HashBiMap<K, V> {

    private final Map<K, V> forwardMap = new HashMap<>();
    private final Map<V, K> backwardMap = new HashMap<>();

    public HashBiMap() {

    }

    public HashBiMap(Map<K, V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) put(entry.getKey(), entry.getValue());
    }

    public void put(K key, V value) {
        if (forwardMap.containsKey(key) || backwardMap.containsKey(value)) {
            throw new IllegalArgumentException("Duplicate key or value");
        }
        forwardMap.put(key, value);
        backwardMap.put(value, key);
    }

    public boolean containsKey(K key) {
        return forwardMap.containsKey(key);
    }

    public boolean containsValue(V value) {
        return backwardMap.containsKey(value);
    }

    public V get(K key) {
        return forwardMap.get(key);
    }

    public K getKey(V value) {
        return backwardMap.get(value);
    }

    public V removeByKey(K key) {
        V value = forwardMap.remove(key);
        if (value != null) {
            backwardMap.remove(value);
        }
        return value;
    }

    public K removeByValue(V value) {
        K key = backwardMap.remove(value);
        if (key != null) {
            forwardMap.remove(key);
        }
        return key;
    }

    public int size() {
        return forwardMap.size();
    }

    public boolean isEmpty() {
        return forwardMap.isEmpty();
    }

    public void clear() {
        forwardMap.clear();
        backwardMap.clear();
    }

    @Override
    public String toString() {
        return "Forward Map: " + forwardMap.toString() + "\nBackward Map: " + backwardMap.toString();
    }


}