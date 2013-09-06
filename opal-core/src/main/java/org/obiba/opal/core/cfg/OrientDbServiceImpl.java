package org.obiba.opal.core.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.persistence.NonUniqueResultException;

import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.tx.OTransaction;
import com.orientechnologies.orient.object.db.OObjectDatabasePool;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;

@Component
public class OrientDbServiceImpl implements Service, OrientDbService {

  private static final Logger log = LoggerFactory.getLogger(OrientDbServiceImpl.class);

  @Value("${OPAL_HOME}/opal-config-db")
  private String path;

  //  @Value("${org.obiba.opal.config.username}")
  private String username = "admin";

  //  @Value("${org.obiba.opal.config.password}")
  private String password = "admin";

  private OServer server;

  @Override
  public boolean isRunning() {
    return server != null;
  }

  @Override
  public void start() {
    try {
      server = OServerMain.create();
      server.startup();
      server.activate();

      // create database
      ODatabase database = new OObjectDatabaseTx(getUrl());
      if(!database.exists()) {
        database.create();
      }
      database.close();

    } catch(Exception e) {
      throw new RuntimeException("Cannot start OrientDB service", e);
    }
  }

  private String getUrl() {
    return "local:" + path;
  }

  @Override
  public void stop() {
    if(server != null) server.shutdown();
  }

  @Override
  public String getName() {
    return "OrientDB";
  }

  @Override
  public <T> T execute(OrientDbTransactionCallback<T> action) {
    OObjectDatabaseTx db = getDatabaseDocumentTx();
    try {
      db.begin(OTransaction.TXTYPE.OPTIMISTIC);
      T t = action.doInTransaction(db);
      db.commit();
      return t;
    } catch(Exception e) {
      db.rollback();
      throw new RuntimeException(e);
    } finally {
      db.close();
    }
  }

  @Override
  public <T> List<T> list(String sql, Map<String, Object> params) {
    OObjectDatabaseTx db = getDatabaseDocumentTx();
    try {
      return db.command(new OSQLSynchQuery(sql)).execute(params);
    } finally {
      db.close();
    }
  }

  @Nullable
  @Override
  public <T> T uniqueResult(String sql, Map<String, Object> params) {
    OObjectDatabaseTx db = getDatabaseDocumentTx();
    try {
      List<T> list = db.command(new OSQLSynchQuery(sql)).execute(params);
      if(list.size() > 1) throw new NonUniqueResultException();
      return list.isEmpty() ? null : list.get(0);
    } finally {
      db.close();
    }
  }

  @Override
  public <T> List<T> list(String sql, String paramName, Object paramValue) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(paramName, paramValue);
    return list(sql, params);
  }

  @Nullable
  @Override
  public <T> T uniqueResult(String sql, String paramName, Object paramValue) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(paramName, paramValue);
    return uniqueResult(sql, params);
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

  private OObjectDatabaseTx getDatabaseDocumentTx() {
    return OObjectDatabasePool.global().acquire(getUrl(), username, password);
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    throw new NoSuchServiceConfigurationException(getName());

  }
}
