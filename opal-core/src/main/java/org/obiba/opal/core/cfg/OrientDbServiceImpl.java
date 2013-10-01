package org.obiba.opal.core.cfg;

import java.util.Arrays;

import javax.annotation.Nullable;
import javax.persistence.NonUniqueResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.index.OIndexManager;
import com.orientechnologies.orient.core.index.ONullOutputListener;
import com.orientechnologies.orient.core.index.OPropertyIndexDefinition;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.tx.OTransaction;
import com.orientechnologies.orient.object.db.OObjectDatabasePool;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import com.orientechnologies.orient.server.OServer;

@Component
public class OrientDbServiceImpl implements OrientDbService {

  private static final Logger log = LoggerFactory.getLogger(OrientDbServiceImpl.class);

  public static final String URL = "local:${OPAL_HOME}/data/orientdb/opal-config";

  @Value(URL)
  private String url;

  //  @Value("${org.obiba.opal.config.username}")
  private String username = "admin";

  //  @Value("${org.obiba.opal.config.password}")
  private String password = "admin";

  private static OServer server;

  //  @PostConstruct
  public static void start() {
    String url = URL.replace("${OPAL_HOME}", System.getProperty("OPAL_HOME"));
    log.info("Start OrientDB server ({})", url);
    System.setProperty("ORIENTDB_HOME", url);
    try {
      server = new OServer() //
          .startup(OrientDbServiceImpl.class.getResourceAsStream("/orientdb-server-config.xml")) //
          .activate();

      // create database if does not exist
      ODatabase database = new OObjectDatabaseTx(url);
      if(!database.exists()) {
        database.create();
      }
      database.close();

    } catch(Exception e) {
      log.error("Cannot start OrientDB server", e);
      throw new RuntimeException("Cannot start OrientDB server", e);
    }
  }

  //  @PreDestroy
  public static void stop() {
//    log.info("Stop OrientDB server ({})", url);
    if(server != null) server.shutdown();
  }

  @Override
  public <T> T execute(OrientDbTransactionCallback<T> action) throws OException {
    OObjectDatabaseTx db = getDatabaseDocumentTx();
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
  public <T> Iterable<T> list(Class<T> clazz) {
    OObjectDatabaseTx db = getDatabaseDocumentTx();
    try {
      return detachAll(db.browseClass(clazz), db);
    } finally {
      db.close();
    }
  }

  @Override
  public <T> Iterable<T> list(String sql, Object... params) {
    OObjectDatabaseTx db = getDatabaseDocumentTx();
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
    OObjectDatabaseTx db = getDatabaseDocumentTx();
    try {
      return db.countClass(clazz);
    } finally {
      db.close();
    }
  }

  @Override
  public void registerEntityClass(Class<?>... classes) {
    OObjectDatabaseTx db = getDatabaseDocumentTx();
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
    OObjectDatabaseTx db = getDatabaseDocumentTx();
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

  private OObjectDatabaseTx getDatabaseDocumentTx() {
    return OObjectDatabasePool.global().acquire(url, username, password);
  }

}
