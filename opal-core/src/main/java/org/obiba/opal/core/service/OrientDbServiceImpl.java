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

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.persistence.NonUniqueResultException;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;

import org.obiba.opal.core.domain.HasUniqueProperties;
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
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.orientechnologies.orient.core.index.OCompositeKey;
import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.command.OCommandOutputListener;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.db.tool.ODatabaseExport;
import com.orientechnologies.orient.core.db.tool.ODatabaseImport;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexManagerProxy;
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

  public DefaultBeanValidator getDefaultBeanValidator() {
    return defaultBeanValidator;
  }

  public void setDefaultBeanValidator(DefaultBeanValidator defaultBeanValidator) {
    this.defaultBeanValidator = defaultBeanValidator;
  }

  @Autowired
  private DefaultBeanValidator defaultBeanValidator;

  public OrientDbServerFactory getServerFactory() {
    return serverFactory;
  }

  public void setServerFactory(OrientDbServerFactory serverFactory) {
    this.serverFactory = serverFactory;
  }

  @Autowired
  private OrientDbServerFactory serverFactory;

  private final Gson gson = new GsonBuilder() //
      .setDateFormat(DATE_PATTERN) //
      .registerTypeAdapter(Date.class, new DateDeserializer()) //
      .create();

  @Override
  public <T> T execute(WithinDocumentTxCallback<T> callback) {
    try(ODatabaseDocumentTx db = serverFactory.getDocumentTx()) {
      try {
        return callback.withinDocumentTx(db);
      } catch(OException e) {
        db.rollback();
        throw e;
      }
    }
  }

  public <T> void saveNonUnique(@NotNull T t) {
    //noinspection ConstantConditions
    Preconditions.checkArgument(t != null, "t cannot be null");
    defaultBeanValidator.validate(t);

    try(ODatabaseDocumentTx db = serverFactory.getDocumentTx()) {
      ODocument document = toDocument(t);
      db.begin(OTransaction.TXTYPE.OPTIMISTIC);
      log.debug("save {}", document);
      document.save();

      db.commit();
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
    Preconditions.checkArgument(!beansByTemplate.isEmpty(), "beansByTemplate cannot be empty");

    for(HasUniqueProperties bean : beansByTemplate.values()) {
      defaultBeanValidator.validate(bean);
    }

    try(ODatabaseDocumentTx db = serverFactory.getDocumentTx()) {
      Iterable<ODocument> documents = getDocuments(db, beansByTemplate);
      db.begin(OTransaction.TXTYPE.OPTIMISTIC);

      for(ODocument document : documents) {
        log.debug("save {}", document);
        document.save();
      }

      db.commit();
    }
  }

  private Iterable<ODocument> getDocuments(ODatabaseDocumentTx db,
      Map<HasUniqueProperties, HasUniqueProperties> beansByTemplate) {
    Collection<ODocument> documents = new ArrayList<>(beansByTemplate.size());
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
    try(ODatabaseDocumentTx db = serverFactory.getDocumentTx()) {
      ODocument document = findUniqueDocument(db, template);
      return document == null ? null : (T) fromDocument(template.getClass(), document);
    }
  }

  @Override
  public String toJson(Object object) {
    return gson.toJson(object);
  }

  @Override
  public <T> T fromJson(String json, Class<T> classOfT) {
    return gson.fromJson(json, classOfT);
  }

  private ODocument findUniqueDocument(ODatabaseDocumentTx db, HasUniqueProperties template) {
    OIndex<?> index = getIndex(db, template);
    OIdentifiable identifiable = (OIdentifiable) index.get(getKey(template));
    return identifiable == null ? null : identifiable.<ODocument>getRecord();
  }

  private OIndex<?> getIndex(ODatabaseDocumentTx db, HasUniqueProperties template) {
    return db.getMetadata().getIndexManager().getIndex(getIndexName(template));
  }

  private OIndex<?> getIndex(ODatabaseDocumentTx db, Class<? extends HasUniqueProperties> clazz) {
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
  public void copyToDocument(Object obj, ORecord document) {
    document.fromJSON(gson.toJson(obj));
  }

  @Override
  public <T> T fromDocument(Class<T> clazz, ORecord document) {
    return gson.fromJson(document.toJSON(), clazz);
  }

  @Override
  public void exportDatabase(File target) throws IOException {
    try(ODatabaseDocumentTx db = serverFactory.getDocumentTx()) {
      ODatabaseExport export = new ODatabaseExport(db, target.getAbsolutePath(), new OCommandOutputListener() {
        @Override
        public void onMessage(String s) {
          log.info(s);
        }
      });

      export.exportDatabase();
      export.close();
    }
  }

  @Override
  public void importDatabase(File source) throws IOException {
    try(ODatabaseDocumentTx db = serverFactory.getDocumentTx()) {
      ODatabaseImport importDb = new ODatabaseImport(db, source.getAbsolutePath(), new OCommandOutputListener() {
        @Override
        public void onMessage(String s) {
          log.info(s);
        }
      });

      importDb.importDatabase();
      importDb.close();
    }

    log.info("Imported database {}", serverFactory.getServer().getDatabaseDirectory());
  }

  @Override
  public void dropDatabase() {
    serverFactory.getDocumentTx().drop();
    log.info("Dropped database {}", serverFactory.getServer().getDatabaseDirectory());
  }

  @Override
  public <T> Iterable<T> list(Class<T> clazz) {
    try(ODatabaseDocumentTx db = serverFactory.getDocumentTx()) {
      return fromDocuments(db.browseClass(clazz.getSimpleName()), clazz);
    }
  }

  @Override
  public <T> Iterable<T> list(Class<T> clazz, String sql, Object... params) {
    try(ODatabaseDocumentTx db = serverFactory.getDocumentTx()) {
      return fromDocuments(db.<List<ODocument>>query(new OSQLSynchQuery<ODocument>(sql), params), clazz);
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
    try(ODatabaseDocumentTx db = serverFactory.getDocumentTx()) {
      Iterable<T> res = Iterables.transform(documents, new Function<ODocument, T>() {
        @Override
        public T apply(ODocument document) {
          return fromDocument(clazz, document);
        }
      });

      return Lists.newArrayList(res); //consume iterable to trigger lazy loading
    }
  }

  @Override
  public <T> long count(Class<T> clazz) {
    try(ODatabaseDocumentTx db = serverFactory.getDocumentTx()) {
      return db.countClass(clazz.getSimpleName());
    }
  }

  @Override
  public void delete(@NotNull HasUniqueProperties... templates) {
    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {

      Collection<ODocument> documents = new ArrayList<>(templates.length);
      for(HasUniqueProperties template : templates) {
        documents.add(findUniqueDocument(db, template));
      }

      db.begin(OTransaction.TXTYPE.OPTIMISTIC);
      for(ODocument document : documents) {
        if (document != null)
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
    HasUniqueProperties bean = BeanUtils.instantiate(clazz);

    try(ODatabaseDocumentTx db = serverFactory.getDocumentTx()) {
      OIndexManagerProxy ixManager = db.getMetadata().getIndexManager();

      if(ixManager.existsIndex(getIndexName(bean))) {
        db.getMetadata().getIndexManager().dropIndex(getIndexName(bean));
      }
    }

    try(ODatabaseDocumentTx db = serverFactory.getDocumentTx()) {
      String className = clazz.getSimpleName();

      OClass indexClass;
      OSchema schema = db.getMetadata().getSchema();

      if(schema.existsClass(className)) {
        indexClass = schema.getClass(className);
      } else {
        indexClass = schema.createClass(className);
        schema.save();
      }

      List<String> uniqueProperties = bean.getUniqueProperties();

      for(String propertyPath : uniqueProperties) {
        OProperty property = indexClass.getProperty(propertyPath);
        if(property == null) {
          PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(clazz, propertyPath);
          Class<?> propertyType = propertyDescriptor.getPropertyType();
          indexClass
              .createProperty(propertyPath, OType.getTypeByClass(propertyType.isEnum() ? String.class : propertyType));
          schema.save();
        }
      }

      indexClass.createIndex(getIndexName(bean), OClass.INDEX_TYPE.UNIQUE,
          uniqueProperties.toArray(new String[uniqueProperties.size()]));
    }
  }

  @Override
  public void createIndex(Class<?> clazz, OClass.INDEX_TYPE indexType, OType type, @NotNull String... propertyPath) {
    //noinspection ConstantConditions
    Preconditions.checkArgument(propertyPath != null, "PropertyPath cannot be null");

    try(ODatabaseDocumentTx db = serverFactory.getDocumentTx()) {
      OIndexManagerProxy ixManager = db.getMetadata().getIndexManager();

      if(ixManager.existsIndex(getIndexName(clazz, Lists.newArrayList(propertyPath)))) {
        db.getMetadata().getIndexManager().dropIndex(getIndexName(clazz, Lists.newArrayList(propertyPath)));
      }
    }

    try(ODatabaseDocumentTx db = serverFactory.getDocumentTx()) {
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

  private class DateDeserializer implements JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonElement jsonElement, Type typeOF, JsonDeserializationContext context)
        throws JsonParseException {

      try {
        return new SimpleDateFormat(DATE_PATTERN).parse(jsonElement.getAsString());
      } catch(ParseException e) {
      }

      try {
        return new Date(jsonElement.getAsLong());
      } catch(NumberFormatException e) {
        throw new JsonParseException("Unparseable date: " + jsonElement.getAsString());
      }
    }
  }
}
