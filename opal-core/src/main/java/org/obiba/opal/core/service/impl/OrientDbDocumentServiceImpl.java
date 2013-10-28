package org.obiba.opal.core.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.NonUniqueResultException;
import javax.validation.ConstraintViolationException;

import org.obiba.opal.core.service.OrientDbServerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.index.OIndexManager;
import com.orientechnologies.orient.core.index.ONullOutputListener;
import com.orientechnologies.orient.core.index.OPropertyIndexDefinition;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.tx.OTransaction;

@Component
public class OrientDbDocumentServiceImpl implements OrientDbDocumentService {

  @Autowired
  private DefaultBeanValidator defaultBeanValidator;

  @Autowired
  private OrientDbServerFactory serverFactory;

  private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

  @Override
  public <T> T execute(WithinDocumentTxCallback<T> callback) {
    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {
      return callback.withinDocumentTx(db);
    } catch(OException e) {
      db.rollback();
      throw e;
    } finally {
      db.close();
    }
  }

  @Override
  public <T> ODocument toDocument(T t) {
    ODocument document = new ODocument(t.getClass().getSimpleName());
    document.fromJSON(gson.toJson(t));
    return document;
  }

  @Override
  public <T> void save(@Nonnull T... t) throws ConstraintViolationException {
    for(T obj : t) {
      defaultBeanValidator.validate(obj);
    }

    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {
      Collection<ODocument> documents = new ArrayList<ODocument>(t.length);
      for(T obj : t) {
        documents.add(toDocument(obj));
      }

      db.begin(OTransaction.TXTYPE.OPTIMISTIC);
      for(ODocument document : documents) {
        document.save();
      }
      db.commit();

    } catch(OException e) {
      db.rollback();
      throw e;
    } finally {
      db.close();
    }
  }

  @Override
  public <T> Iterable<T> list(Class<T> clazz) {
    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {
      return fromDocuments(db.browseClass(clazz.getSimpleName()), clazz);
    } finally {
      db.close();
    }
  }

  @Override
  public <T> Iterable<T> list(Class<T> clazz, String sql, Object... params) {
    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {
      return fromDocuments(db.<List<ODocument>>query(new OSQLSynchQuery<ODocument>(sql), params), clazz);
    } finally {
      db.close();
    }
  }

  @Nullable
  @Override
  public <T> T uniqueResult(Class<T> clazz, String sql, Object... params) {
    try {
      return (T) Iterables.getOnlyElement(list(clazz, sql, params), null);
    } catch(IllegalArgumentException e) {
      throw new NonUniqueResultException(
          "Non unique result for query '" + sql + "' with args: " + Arrays.asList(params));
    }
  }

  private <T> Iterable<T> fromDocuments(Iterable<ODocument> documents, final Class<T> clazz) {
    return Iterables.transform(documents, new Function<ODocument, T>() {
      @Override
      public T apply(ODocument document) {
        return gson.fromJson(document.toJSON(), clazz);
      }
    });
  }

  @Override
  public <T> long count(Class<T> clazz) {
    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {
      return db.countClass(clazz.getSimpleName());
    } finally {
      db.close();
    }
  }

  @Override
  public void delete(String sql, Object... params) {
    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {
      List<ODocument> documents = db.query(new OSQLSynchQuery<ODocument>(sql), params);

      db.begin(OTransaction.TXTYPE.OPTIMISTIC);
      for(ODocument document : documents) {
        document.delete();
      }
      db.commit();

    } catch(OException e) {
      db.rollback();
      throw e;
    } finally {
      db.close();
    }
  }

  @Override
  public void createIndex(Class<?> clazz, String propertyPath, OClass.INDEX_TYPE indexType, OType type) {
    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {
      String className = clazz.getSimpleName();
      int clusterId = db.getClusterIdByName(className.toLowerCase());
      OIndexManager indexManager = db.getMetadata().getIndexManager();
      indexManager.createIndex(className + "." + propertyPath, indexType.name(),
          new OPropertyIndexDefinition(className, propertyPath, type), new int[] { clusterId },
          ONullOutputListener.INSTANCE);
    } finally {
      db.close();
    }
  }

  @Override
  public void createUniqueIndex(Class<?> clazz, String propertyPath, OType type) {
    createIndex(clazz, propertyPath, OClass.INDEX_TYPE.UNIQUE, type);
  }

  @Override
  public void createUniqueStringIndex(Class<?> clazz, String propertyPath) {
    createUniqueIndex(clazz, propertyPath, OType.STRING);
  }
}
