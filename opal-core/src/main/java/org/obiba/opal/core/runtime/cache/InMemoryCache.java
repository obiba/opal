package org.obiba.opal.core.runtime.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Spring Cache implementation backed by Guava Cache
 */
public class InMemoryCache extends AbstractValueAdaptingCache {

  private final String name;
  private final Cache<Object, Object> cache;

  public InMemoryCache(String name, Cache<Object, Object> cache) {
    super(true); // allowNullValues
    this.name = name;
    this.cache = cache;
  }

  /**
   * Convenience constructor with common cache settings
   */
  public InMemoryCache(String name, long maxSize, long expireAfterWriteMinutes) {
    super(true);
    this.name = name;
    this.cache = CacheBuilder.newBuilder()
        .maximumSize(maxSize)
        .expireAfterWrite(expireAfterWriteMinutes, TimeUnit.MINUTES)
        .recordStats()
        .build();
  }

  @NotNull
  @Override
  public String getName() {
    return this.name;
  }

  @NotNull
  @Override
  public Object getNativeCache() {
    return this.cache;
  }

  @Override
  protected Object lookup(@NotNull Object key) {
    return cache.getIfPresent(key);
  }

  @Override
  public <T> T get(@NotNull Object key, @NotNull Callable<T> valueLoader) {
    try {
      return (T) cache.get(key, () -> {
        T value = valueLoader.call();
        return toStoreValue(value);
      });
    } catch (Exception e) {
      throw new ValueRetrievalException(key, valueLoader, e.getCause());
    }
  }

  @Override
  public void put(@NotNull Object key, Object value) {
    cache.put(key, toStoreValue(value));
  }

  @Override
  public ValueWrapper putIfAbsent(@NotNull Object key, Object value) {
    Object existing = cache.asMap().putIfAbsent(key, toStoreValue(value));
    return toValueWrapper(existing);
  }

  @Override
  public void evict(@NotNull Object key) {
    cache.invalidate(key);
  }

  @Override
  public boolean evictIfPresent(@NotNull Object key) {
    boolean present = cache.getIfPresent(key) != null;
    cache.invalidate(key);
    return present;
  }

  @Override
  public void clear() {
    cache.invalidateAll();
  }

  /**
   * Get cache statistics
   */
  public com.google.common.cache.CacheStats getStats() {
    return cache.stats();
  }
}

