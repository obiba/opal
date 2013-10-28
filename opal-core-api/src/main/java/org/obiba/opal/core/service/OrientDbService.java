package org.obiba.opal.core.service;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolationException;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;

public interface OrientDbService {

  <T> T save(T t) throws ConstraintViolationException;

  void delete(Object obj);

  <T> T execute(OrientDbTransactionCallback<T> action);

  void registerEntityClass(Class<?>... classes);

  <T> Iterable<T> list(String sql, Object... params);

  @Nullable
  <T> T uniqueResult(String sql, Object... params);

  long count(Class<?> clazz);

  <T> Iterable<T> list(Class<T> clazz);

  /**
   * Create index.
   * Be careful to not create index on Abstract class or it will fail
   *
   * @param clazz
   * @param property
   * @param indexType
   * @param type
   */
  void createIndex(Class<?> clazz, String property, OClass.INDEX_TYPE indexType, OType type);

  void createUniqueIndex(Class<?> clazz, String property, OType type);

  void createUniqueStringIndex(Class<?> clazz, String property);
}
