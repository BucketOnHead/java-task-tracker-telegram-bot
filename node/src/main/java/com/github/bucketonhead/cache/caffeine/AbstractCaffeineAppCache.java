package com.github.bucketonhead.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.bucketonhead.cache.AppCache;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractCaffeineAppCache<K, V> implements AppCache<K, V> {
    private final Cache<K, V> cache;

    @Override
    public Optional<V> get(K key) {
        V cachedValue = cache.getIfPresent(key);
        if (cachedValue != null) {
            log.info("Value found in cache: key={}", key);
            log.debug("Value found in cache: {}", cachedValue);
        } else {
            log.info("Value not found in cache: key={}", key);
        }

        return Optional.ofNullable(cachedValue);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
        log.info("Value added/updated: key={}", key);
        log.debug("Value added/updated: {}", value);
    }

    @Override
    public boolean contains(K key) {
        boolean contains = cache.getIfPresent(key) != null;
        if (contains) {
            log.info("Value found in cache: key={}", key);
        } else {
            log.info("Value not found in cache: key={}", key);
        }

        return contains;
    }

    @Override
    public void remove(K key) {
        cache.invalidate(key);
        log.info("Value removed from cache: key={}", key);
    }
}
