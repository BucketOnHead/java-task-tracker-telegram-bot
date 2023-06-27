package com.github.bucketonhead.cache;

import java.util.Optional;

public interface AppCache<K, V> {

    Optional<V> get(K key);

    void put(V value);

    boolean contains(K key);

    void remove(K key);
}
