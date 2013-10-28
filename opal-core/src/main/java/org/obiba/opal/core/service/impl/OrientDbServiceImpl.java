package org.obiba.opal.core.service.impl;

import java.util.Arrays;

import javax.annotation.Nullable;
import javax.persistence.NonUniqueResultException;
import javax.validation.ConstraintViolationException;

import org.obiba.opal.core.service.OrientDbServerFactory;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.OrientDbTransactionCallback;
import org.obiba.opal.core.service.OrientDbTransactionCallbackWithoutResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.index.OIndexManager;
import com.orientechnologies.orient.core.index.ONullOutputListener;
import com.orientechnologies.orient.core.index.OPropertyIndexDefinition;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.tx.OTransaction;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

@Component
public class OrientDbServiceImpl implements OrientDbService {

//  private static final Logger log = LoggerFactory.getLogger(OrientDbServiceImpl.class);

  @Autowired
  private DefaultBeanValidator defaultBeanValidator;

  @Autowired
  private OrientDbServerFactory serverFactory;

  @Override
  public <T> T execute(OrientDbTransactionCallback<T> action) {
    OObjectDatabaseTx db = serverFactory.getObjectTx();
    try {
      db.begin(OTransaction.TXTYPE.OPTIMISTIC);
      T t = action.doInTransaction(db);
      db.commit();
      return t;
    } catch(OException e) {
      db.rollback();
      throw e;
    } finally {
      db.close();
    }
  }

  @Override
  public <T> T save(final T t) throws ConstraintViolationException {
    defaultBeanValidator.validate(t);
    return execute(new OrientDbTransactionCallback<T>() {
      @Override
      public T doInTransaction(OObjectDatabaseTx db) {
        return db.save(t);
      }
    });
  }

  @Override
  public void delete(final Object obj) {
    execute(new OrientDbTransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(OObjectDatabaseTx db) {
        db.delete(obj);
      }
    });
  }

  @Override
  public <T> Iterable<T> list(Class<T> clazz) {
    OObjectDatabaseTx db = serverFactory.getObjectTx();
    try {
      return detachAll(db.browseClass(clazz), db);
    } finally {
      db.close();
    }
  }

  @Override
  public <T> Iterable<T> list(String sql, Object... params) {
    OObjectDatabaseTx db = serverFactory.getObjectTx();
    try {
      return detachAll(db.command(new OSQLSynchQuery(sql)).<Iterable<T>>execute(params), db);
    } finally {
      db.close();
    }
  }

  private <T> Iterable<T> detachAll(Iterable<T> iterable, final OObjectDatabaseTx db) {
    return Iterables.transform(iterable, new Function<T, T>() {
      @Override
      public T apply(T input) {
        return db.detach(input, true);
      }
    });
  }

  @SuppressWarnings("unchecked")
  @Nullable
  @Override
  public <T> T uniqueResult(String sql, Object... params) {
    try {
      return (T) Iterables.getOnlyElement(list(sql, params), null);
    } catch(IllegalArgumentException e) {
      throw new NonUniqueResultException(
          "Non unique result for query '" + sql + "' with args: " + Arrays.asList(params));
    }
  }

  @Override
  public long count(Class<?> clazz) {
    OObjectDatabaseTx db = serverFactory.getObjectTx();
    try {
      return db.countClass(clazz);
    } finally {
      db.close();
    }
  }

  @Override
  public void registerEntityClass(Class<?>... classes) {
    OObjectDatabaseTx db = serverFactory.getObjectTx();
    try {
      for(Class<?> clazz : classes) {
        db.getEntityManager().registerEntityClass(clazz);
      }
    } finally {
      db.close();
    }
  }

  @Override
  public void createIndex(Class<?> clazz, String property, OClass.INDEX_TYPE indexType, OType type) {
    OObjectDatabaseTx db = serverFactory.getObjectTx();
    try {
      String className = clazz.getSimpleName();
      int clusterId = db.getClusterIdByName(className.toLowerCase());
      OIndexManager indexManager = db.getMetadata().getIndexManager();
      indexManager.createIndex(className + "." + property, indexType.name(),
          new OPropertyIndexDefinition(className, property, type), new int[] { clusterId },
          ONullOutputListener.INSTANCE);
    } finally {
      db.close();
    }
  }

  @Override
  public void createUniqueIndex(Class<?> clazz, String property, OType type) {
    createIndex(clazz, property, OClass.INDEX_TYPE.UNIQUE, type);
  }

  @Override
  public void createUniqueStringIndex(Class<?> clazz, String property) {
    createUniqueIndex(clazz, property, OType.STRING);
  }

  @VisibleForTesting
  public void setServerFactory(OrientDbServerFactory serverFactory) {
    this.serverFactory = serverFactory;
  }
}
