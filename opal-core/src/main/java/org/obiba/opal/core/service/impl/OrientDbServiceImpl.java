package org.obiba.opal.core.service.impl;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.persistence.NonUniqueResultException;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;

import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.service.OrientDbServerFactory;
import org.obiba.opal.core.service.OrientDbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orientechnologies.common.collection.OCompositeKey;
import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.db.ODatabaseComplex;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.tx.OTransaction;

@Component
public class OrientDbServiceImpl implements OrientDbService {

  private static final Logger log = LoggerFactory.getLogger(OrientDbServiceImpl.class);

  private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

  @Autowired
  private DefaultBeanValidator defaultBeanValidator;

  @Autowired
  private OrientDbServerFactory serverFactory;

  private final Gson gson = new GsonBuilder().setDateFormat(DATE_PATTERN).create();

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

  public <T> void saveNonUnique(@NotNull T t) {
    //noinspection ConstantConditions
    Preconditions.checkArgument(t != null, "t cannot be null");

    defaultBeanValidator.validate(t);

    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {

      ODocument document = toDocument(t);
      db.begin(OTransaction.TXTYPE.OPTIMISTIC);
      log.debug("save {}", document);
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
  public void save(@Nullable HasUniqueProperties template, @NotNull HasUniqueProperties hasUniqueProperties)
      throws ConstraintViolationException {
    //noinspection ConstantConditions
    Preconditions.checkArgument(hasUniqueProperties != null, "hasUniqueProperties cannot be null");
    save(ImmutableMap.of(template == null ? hasUniqueProperties : template, hasUniqueProperties));
  }

  @Override
  public void save(@NotNull Map<HasUniqueProperties, HasUniqueProperties> beansByTemplate)
      throws ConstraintViolationException {
    //noinspection ConstantConditions
    Preconditions.checkArgument(beansByTemplate != null, "beansByTemplate cannot be null");
    Preconditions.checkArgument(beansByTemplate.size() > 0, "beansByTemplate cannot be empty");

    for(HasUniqueProperties bean : beansByTemplate.values()) {
      defaultBeanValidator.validate(bean);
    }

    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {

      Iterable<ODocument> documents = getDocuments(db, beansByTemplate);

      db.begin(OTransaction.TXTYPE.OPTIMISTIC);
      for(ODocument document : documents) {
        log.debug("save {}", document);
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

  private Iterable<ODocument> getDocuments(ODatabaseComplex<?> db,
      Map<HasUniqueProperties, HasUniqueProperties> beansByTemplate) {
    Collection<ODocument> documents = new ArrayList<ODocument>(beansByTemplate.size());
    for(Map.Entry<HasUniqueProperties, HasUniqueProperties> entry : beansByTemplate.entrySet()) {
      ODocument document = findUniqueDocument(db, entry.getKey());
      if(document == null) {
        document = toDocument(entry.getValue());
      } else {
        copyToDocument(entry.getValue(), document);
      }
      documents.add(document);
    }
    return documents;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends HasUniqueProperties> T findUnique(@NotNull HasUniqueProperties template) {
    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {
      ODocument document = findUniqueDocument(db, template);
      return document == null ? null : (T) fromDocument(template.getClass(), document);
    } finally {
      db.close();
    }
  }

  private ODocument findUniqueDocument(ODatabaseComplex<?> db, HasUniqueProperties template) {
    OIndex<?> index = getIndex(db, template);
    OIdentifiable identifiable = (OIdentifiable) index.get(getKey(template));
    return identifiable == null ? null : identifiable.<ODocument>getRecord();
  }

  private OIndex<?> getIndex(ODatabaseComplex<?> db, HasUniqueProperties template) {
    return db.getMetadata().getIndexManager().getIndex(getIndexName(template));
  }

  private OIndex<?> getIndex(ODatabaseComplex<?> db, Class<? extends HasUniqueProperties> clazz) {
    String indexName = getIndexName(clazz);
    return db.getMetadata().getIndexManager().getIndex(indexName);
  }

  private Object getKey(HasUniqueProperties template) {
    return template.getUniqueValues().size() == 1
        ? template.getUniqueValues().get(0)
        : new OCompositeKey(template.getUniqueValues());
  }

  private <T> ODocument toDocument(T t) {
    ODocument document = new ODocument(t.getClass().getSimpleName());
    copyToDocument(t, document);
    return document;
  }

  @Override
  public void copyToDocument(Object obj, ORecord<?> document) {
    document.fromJSON(gson.toJson(obj));
  }

  @Override
  public <T> T fromDocument(Class<T> clazz, ORecord<?> document) {
    return gson.fromJson(document.toJSON(), clazz);
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
  public void delete(@NotNull HasUniqueProperties... templates) {
    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {

      Collection<ODocument> documents = new ArrayList<ODocument>(templates.length);
      for(HasUniqueProperties template : templates) {
        documents.add(findUniqueDocument(db, template));
      }

      db.begin(OTransaction.TXTYPE.OPTIMISTIC);
      for(ODocument document : documents) {
        db.delete(document);
      }
      for(HasUniqueProperties template : templates) {
        getIndex(db, template).remove(getKey(template));
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
  public void deleteAll(@NotNull Class<? extends HasUniqueProperties> clazz) {
    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {

      db.begin(OTransaction.TXTYPE.OPTIMISTIC);
      for(ODocument document : db.browseClass(clazz.getSimpleName())) {
        db.delete(document);
      }
      getIndex(db, clazz).clear();
      db.commit();

    } catch(OException e) {
      db.rollback();
      throw e;
    } finally {
      db.close();
    }
  }

  @Override
  public void createUniqueIndex(@NotNull Class<? extends HasUniqueProperties> clazz) {
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

      HasUniqueProperties bean = BeanUtils.instantiate(clazz);
      List<String> uniqueProperties = bean.getUniqueProperties();
      for(String propertyPath : uniqueProperties) {
        OProperty property = indexClass.getProperty(propertyPath);
        if(property == null) {
          PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(clazz, propertyPath);
          indexClass.createProperty(propertyPath, OType.getTypeByClass(propertyDescriptor.getPropertyType()));
          schema.save();
        }
      }

      indexClass.createIndex(getIndexName(bean), OClass.INDEX_TYPE.UNIQUE,
          uniqueProperties.toArray(new String[uniqueProperties.size()]));

    } finally {
      db.close();
    }
  }

  @Override
  public void createIndex(Class<?> clazz, OClass.INDEX_TYPE indexType, OType type, @NotNull String... propertyPath) {
    //noinspection ConstantConditions
    Preconditions.checkArgument(propertyPath != null, "PropertyPath cannot be null");
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
      indexClass.createIndex(getIndexName(clazz, Lists.newArrayList(propertyPath)), indexType, propertyPath);

    } finally {
      db.close();
    }
  }

  private String getIndexName(Class<? extends HasUniqueProperties> clazz) {
    return getIndexName(BeanUtils.instantiate(clazz));
  }

  private String getIndexName(HasUniqueProperties hasUniqueProperties) {
    List<String> properties = hasUniqueProperties.getUniqueProperties();
    return getIndexName(hasUniqueProperties.getClass(), properties);
  }

  private String getIndexName(Class<?> clazz, @NotNull Iterable<String> uniqueProperties) {
    StringBuilder indexName = new StringBuilder(clazz.getSimpleName());
    for(String prop : uniqueProperties) {
      indexName.append(".").append(prop);
    }
    return indexName.toString();
  }
}
