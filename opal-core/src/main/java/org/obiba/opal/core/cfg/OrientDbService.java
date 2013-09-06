package org.obiba.opal.core.cfg;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public interface OrientDbService {

  <T> T execute(OrientDbTransactionCallback<T> action);

  void registerEntityClass(Class<?>... classes);

  <T> List<T> list(String sql, Map<String, Object> params);

  <T> List<T> list(String sql, String paramName, Object paramValue);

  @Nullable
  <T> T uniqueResult(String sql, Map<String, Object> params);

  @Nullable
  <T> T uniqueResult(String sql, String paramName, Object paramValue);

}
