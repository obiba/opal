package org.obiba.opal.core.service;

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

  void save(@Nonnull HasUniqueProperties... hasUniqueProperties) throws ConstraintViolationException;

  void delete(@Nonnull HasUniqueProperties... templates);

  void delete(@Nonnull String sql, Object... params);

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
