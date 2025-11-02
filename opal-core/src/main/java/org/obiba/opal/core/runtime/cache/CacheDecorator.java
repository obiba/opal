package org.obiba.opal.core.runtime.cache;

import org.obiba.magma.Datasource;
import org.obiba.magma.Decorator;
import org.obiba.magma.support.CachedDatasource;
import org.springframework.cache.CacheManager;

import java.util.Objects;
import java.util.UUID;

public class CacheDecorator implements Decorator<Datasource> {

  private final CacheManager cacheManager;

  public CacheDecorator(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  @Override
  public Datasource decorate(Datasource datasource) {
    try {
      // ugly way to exclude transient datasources
      UUID.fromString(datasource.getName());
      return datasource;
    } catch (Exception e) {
      return new CachedDatasource(datasource, cacheManager);
    }
  }

  @Override
  public void release(Datasource object) {
  }
}
