/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ORecord;
import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import org.obiba.opal.core.domain.HasUniqueProperties;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface OrientDbService {

  <T> T execute(WithinDocumentTxCallback<T> callback);

  <T> Iterable<T> list(Class<T> clazz);

  <T> Iterable<T> list(Class<T> clazz, String sql, Object... params);

  <T> long count(Class<T> clazz);

  @Nullable
  <T> T uniqueResult(Class<T> clazz, String sql, Object... params);

  <T extends HasUniqueProperties> T findUnique(@NotNull HasUniqueProperties template);

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
  void save(@Nullable HasUniqueProperties template, @NotNull HasUniqueProperties hasUniqueProperties)
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
   * @param beansByTemplate
   * @throws ConstraintViolationException
   */
  void save(@NotNull Map<HasUniqueProperties, HasUniqueProperties> beansByTemplate) throws ConstraintViolationException;

  void delete(@NotNull HasUniqueProperties... templates);

  void deleteAll(@NotNull Class<? extends HasUniqueProperties> clazz);

  void createUniqueIndex(@NotNull Class<? extends HasUniqueProperties> clazz);

  void createIndex(Class<?> clazz, OClass.INDEX_TYPE indexType, OType type, @NotNull String... propertyPath);

  void copyToDocument(Object obj, ORecord document);

  <T> T fromDocument(Class<T> clazz, ORecord document);

  void exportDatabase(File target) throws IOException;

  void importDatabase(File source) throws IOException;

  void dropDatabase();

  String toJson(Object object);

  <T> T fromJson(String json, Class<T> classOfT);

  interface WithinDocumentTxCallback<T> {

    T withinDocumentTx(ODatabaseDocument db);

  }

  abstract class WithinDocumentTxCallbackWithoutResult implements WithinDocumentTxCallback<Void> {

    @Override
    public final Void withinDocumentTx(ODatabaseDocument db) {
      withinDocumentTxWithoutResult(db);
      return null;
    }

    protected abstract void withinDocumentTxWithoutResult(ODatabaseDocument db);
  }
}
