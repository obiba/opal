package org.obiba.opal.core.service;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.ConstraintViolationException;

import org.obiba.opal.core.domain.HasUniqueProperties;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;

public interface OrientDbService {

  <T> T execute(WithinDocumentTxCallback<T> callback);

  <T> Iterable<T> list(Class<T> clazz);

  <T> Iterable<T> list(Class<T> clazz, String sql, Object... params);

  <T> long count(Class<T> clazz);

  @Nullable
  <T> T uniqueResult(Class<T> clazz, String sql, Object... params);

  <T extends HasUniqueProperties> T findUnique(@Nonnull HasUniqueProperties template);

  /**
   * Find <b>template</b> document and save it with <b>hasUniqueProperties</b> properties:
   * <ul>
   * <li>search document corresponding to <b>template</b> unique keys</li>
   * <li>create and save a new document based on <b>hasUniqueProperties</b> if not found</li>
   * <li>update existing <b>template</b> document with <b>hasUniqueProperties</b> properties</li>
   * </ul>
   *
   * @param template
   * @param hasUniqueProperties
   * @throws ConstraintViolationException
   */
  void save(@Nullable HasUniqueProperties template, @Nonnull HasUniqueProperties hasUniqueProperties)
      throws ConstraintViolationException;

  /**
   * Find documents defined by <b>beansByTemplate</b> keys and save them with <b>beansByTemplate</b> values.
   * For each map entry:
   * <ul>
   * <li>search document corresponding to <b>template key</b> unique keys</li>
   * <li>create and save a new document based on <b>hasUniqueProperties value</b> if not found</li>
   * <li>update existing <b>template key</b> document with <b>hasUniqueProperties value</b> properties</li>
   * </ul>
   *
   * @param template
   * @param hasUniqueProperties
   * @throws ConstraintViolationException
   */
  void save(@Nonnull Map<HasUniqueProperties, HasUniqueProperties> beansByTemplate) throws ConstraintViolationException;

  void delete(@Nonnull HasUniqueProperties... templates);

  void deleteAll(@Nonnull Class<? extends HasUniqueProperties> clazz);

  void createUniqueIndex(@Nonnull Class<? extends HasUniqueProperties> clazz);

  void createIndex(Class<?> clazz, OClass.INDEX_TYPE indexType, OType type, @Nonnull String... propertyPath);

  interface WithinDocumentTxCallback<T> {

    T withinDocumentTx(ODatabaseDocumentTx db);

  }

  abstract class WithinDocumentTxCallbackWithoutResult implements WithinDocumentTxCallback<Void> {

    @Override
    public final Void withinDocumentTx(ODatabaseDocumentTx db) {
      withinDocumentTxWithoutResult(db);
      return null;
    }

    protected abstract void withinDocumentTxWithoutResult(ODatabaseDocumentTx db);
  }
}
