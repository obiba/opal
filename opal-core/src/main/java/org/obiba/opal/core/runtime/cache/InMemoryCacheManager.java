package org.obiba.opal.core.runtime.cache;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * CacheManager implementation for Guava Cache
 */
public class InMemoryCacheManager implements CacheManager {

  private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>();
  private final long defaultMaxSize;
  private final long defaultExpireAfterWriteMinutes;

  public InMemoryCacheManager() {
    this(10000, 60); // Default: 10k entries, 60 min expiry
  }

  public InMemoryCacheManager(long defaultMaxSize, long defaultExpireAfterWriteMinutes) {
    this.defaultMaxSize = defaultMaxSize;
    this.defaultExpireAfterWriteMinutes = defaultExpireAfterWriteMinutes;
  }

  @Override
  public Cache getCache(@NotNull String name) {
    return cacheMap.computeIfAbsent(name,
        cacheName -> new InMemoryCache(cacheName, defaultMaxSize, defaultExpireAfterWriteMinutes));
  }

  @NotNull
  @Override
  public Collection<String> getCacheNames() {
    return cacheMap.keySet();
  }

  /**
   * Create a cache with custom configuration
   */
  public Cache createCache(String name, com.google.common.cache.Cache<Object, Object> guavaCache) {
    InMemoryCache cache = new InMemoryCache(name, guavaCache);
    cacheMap.put(name, cache);
    return cache;
  }
}
