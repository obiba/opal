package org.obiba.opal.core.service.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.NonUniqueResultException;
import javax.validation.ConstraintViolationException;

import org.obiba.opal.core.service.OrientDbServerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
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
    copyToDocument(t, document);
    return document;
  }

  @Override
  public <T> void copyToDocument(T t, ODocument document) {
    document.fromJSON(gson.toJson(t));
  }

  @Override
  public <T> T fromDocument(Class<T> clazz, ODocument document) {
    return gson.fromJson(document.toJSON(), clazz);
  }

  @Override
  public <T> void save(@Nonnull T t, @Nonnull String... uniqueProperties) throws ConstraintViolationException {

    defaultBeanValidator.validate(t);

    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {
      BeanWrapperImpl beanWrapper = new BeanWrapperImpl(t);
      Map<String, Object> uniquePropertyValues = new HashMap<String, Object>(uniqueProperties.length);
      for(String uniqueProperty : uniqueProperties) {
        uniquePropertyValues.put(uniqueProperty, beanWrapper.getPropertyValue(uniqueProperty));
      }
      ODocument document = findUnique(db, t.getClass(), uniquePropertyValues);
      if(document == null) {
        document = toDocument(t);
      } else {
        copyToDocument(t, document);
      }

      db.begin(OTransaction.TXTYPE.OPTIMISTIC);
      document.save();
      db.commit();

    } catch(OException e) {
      db.rollback();
      throw e;
    } finally {
      db.close();
    }
  }

  @Override
  public <T> T findUnique(Class<T> clazz, String uniqueProperty, Object uniqueValue) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(uniqueProperty, uniqueValue);
    return findUnique(clazz, map);
  }

  @Override
  public <T> T findUnique(Class<T> clazz, Map<String, Object> uniquePropertyValues) {
    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {
      ODocument document = findUnique(db, clazz, uniquePropertyValues);
      return document == null ? null : fromDocument(clazz, document);
    } finally {
      db.close();
    }
  }

  @Override
  public <T> ODocument findUnique(ODatabaseDocumentTx db, Class<T> clazz, String uniqueProperty, Object uniqueValue) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(uniqueProperty, uniqueValue);
    return findUnique(db, clazz, map);
  }

  @Override
  public <T> ODocument findUnique(ODatabaseDocumentTx db, Class<T> clazz, Map<String, Object> uniquePropertyValues) {
    Set<String> uniqueProperties = uniquePropertyValues.keySet();
    OIndex<?> index = db.getMetadata().getIndexManager()
        .getIndex(getIndexName(clazz, uniqueProperties.toArray(new String[uniqueProperties.size()])));
    Collection<Object> values = uniquePropertyValues.values();
    OIdentifiable identifiable = (OIdentifiable) index
        .get(values.size() > 1 ? values : Iterables.getOnlyElement(values));
    return identifiable == null ? null : identifiable.<ODocument>getRecord();
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
      return Iterables.getOnlyElement(list(clazz, sql, params), null);
    } catch(IllegalArgumentException e) {
      throw new NonUniqueResultException(
          "Non unique result for query '" + sql + "' with args: " + Arrays.asList(params));
    }
  }

  private <T> Iterable<T> fromDocuments(Iterable<ODocument> documents, final Class<T> clazz) {
    return Iterables.transform(documents, new Function<ODocument, T>() {
      @Override
      public T apply(ODocument document) {
        return fromDocument(clazz, document);
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
  public <T> void deleteUnique(Class<T> clazz, String uniqueProperty, Object uniqueValue) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(uniqueProperty, uniqueValue);
    deleteUnique(clazz, map);
  }

  @Override
  public <T> void deleteUnique(Class<T> clazz, Map<String, Object> uniquePropertyValues) {
    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {

      ODocument document = findUnique(db, clazz, uniquePropertyValues);
      db.begin(OTransaction.TXTYPE.OPTIMISTIC);
      document.delete();
      db.commit();

    } catch(OException e) {
      db.rollback();
      throw e;
    } finally {
      db.close();
    }
  }

  @Override
  public void createIndex(Class<?> clazz, OClass.INDEX_TYPE indexType, OType type, @Nonnull String... propertyPath) {
    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {
      String className = clazz.getSimpleName();

      OClass indexClass;
      OSchema schema = db.getMetadata().getSchema();
      if(schema.existsClass(className)) {
        indexClass = schema.getClass(className);
      } else {
        indexClass = schema.createClass(className);
        schema.save();
      }

      for(String prop : propertyPath) {
        OProperty property = indexClass.getProperty(prop);
        if(property == null) {
          indexClass.createProperty(prop, type);
          schema.save();
        }
      }
      indexClass.createIndex(getIndexName(clazz, propertyPath), indexType, propertyPath);

    } finally {
      db.close();
    }
  }

  @Override
  public void createUniqueIndex(Class<?> clazz, OType type, @Nonnull String... propertyPath) {
    createIndex(clazz, OClass.INDEX_TYPE.UNIQUE, type, propertyPath);
  }

  @Override
  public void createUniqueStringIndex(Class<?> clazz, @Nonnull String... propertyPath) {
    createUniqueIndex(clazz, OType.STRING, propertyPath);
  }

  @Override
  public String getIndexName(Class<?> clazz, @Nonnull String... propertyPath) {
    StringBuilder indexName = new StringBuilder(clazz.getSimpleName());
    for(String prop : propertyPath) {
      indexName.append(".").append(prop);
    }
    return indexName.toString();
  }
}
