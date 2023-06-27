package com.github.bucketonhead.cache;

import java.util.Optional;

public interface AppCache<K, V> {

    Optional<V> get(K key);

    void put(K key, V value);

    default void put(V value) {
        K key = getKey(value);
        if (key == null) {
            var msg = "Saving entity only by value is not provided or not implemented";
            throw new RuntimeException(msg);
        }

        put(key, value);
    }

    default K getKey(V value) {
        return null;
    }

    boolean contains(K key);

    void remove(K key);
}
