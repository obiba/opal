package org.obiba.opal.core.service.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.ConstraintViolationException;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

public interface OrientDbDocumentService {

  <T> T execute(WithinDocumentTxCallback<T> callback);

  <T> void save(@Nonnull T... t) throws ConstraintViolationException;

  <T> Iterable<T> list(Class<T> clazz);

  <T> Iterable<T> list(Class<T> clazz, String sql, Object... params);

  <T> long count(Class<T> clazz);

  @Nullable
  <T> T uniqueResult(Class<T> clazz, String sql, Object... params);

  void createUniqueStringIndex(Class<?> clazz, String propertyPath);

  void createUniqueIndex(Class<?> clazz, String propertyPath, OType type);

  void createIndex(Class<?> clazz, String propertyPath, OClass.INDEX_TYPE indexType, OType type);

  /**
   * Must be called within an opened connection but <b>outside</b> the transaction!
   */
  <T> ODocument toDocument(T t);

  void delete(String sql, Object... params);

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
