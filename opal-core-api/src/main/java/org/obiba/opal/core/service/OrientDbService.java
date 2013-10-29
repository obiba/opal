package org.obiba.opal.core.service;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.ConstraintViolationException;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

public interface OrientDbService {

  <T> T execute(WithinDocumentTxCallback<T> callback);

  <T> Iterable<T> list(Class<T> clazz);

  <T> Iterable<T> list(Class<T> clazz, String sql, Object... params);

  <T> long count(Class<T> clazz);

  @Nullable
  <T> T uniqueResult(Class<T> clazz, String sql, Object... params);

  void createUniqueStringIndex(Class<?> clazz, @Nonnull String... propertyPath);

  void createUniqueIndex(Class<?> clazz, OType type, @Nonnull String... propertyPath);

  void createIndex(Class<?> clazz, OClass.INDEX_TYPE indexType, OType type, @Nonnull String... propertyPath);

  /**
   * Must be called <b>within</b> an opened connection but <b>outside</b> the transaction!
   */
  <T> ODocument toDocument(T t);

  void delete(String sql, Object... params);

  <T> void deleteUnique(Class<T> clazz, String uniqueProperty, Object uniqueValue);

  <T> T fromDocument(Class<T> clazz, ODocument document);

  <T> void copyToDocument(T t, ODocument document);

  <T> T findUnique(Class<T> clazz, String uniqueProperty, Object uniqueValue);

  <T> ODocument findUnique(ODatabaseDocumentTx db, Class<T> clazz, String uniqueProperty, Object uniqueValue);

  String getIndexName(Class<?> clazz, @Nonnull String... propertyPath);

  <T> ODocument findUnique(ODatabaseDocumentTx db, Class<T> clazz, Map<String, Object> uniquePropertyValues);

  <T> T findUnique(Class<T> clazz, Map<String, Object> uniquePropertyValues);

  <T> void save(@Nonnull T t, @Nonnull String uniqueProperty, String... otherUniqueProperties)
      throws ConstraintViolationException;

  <T> void deleteUnique(Class<T> clazz, Map<String, Object> uniquePropertyValues);

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
