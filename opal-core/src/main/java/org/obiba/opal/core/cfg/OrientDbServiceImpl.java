package org.obiba.opal.core.cfg;

import java.util.List;
import java.util.Map;

import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.tx.OTransaction;
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
      ODatabaseDocumentTx documentTx = new ODatabaseDocumentTx(getUrl());
      if(!documentTx.exists()) {
        ODatabaseDocumentTx db = documentTx.create();
      }

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
  public <T> T execute(OrientTransactionCallback<T> action) {
    ODatabaseDocumentTx db = getDatabaseDocumentTx();
    try {
      db.begin(OTransaction.TXTYPE.OPTIMISTIC);
      T t = action.doInTransaction();
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
  public List<ODocument> query(OSQLSynchQuery<ODocument> query, Map<String, Object> params) {
    ODatabaseDocumentTx db = getDatabaseDocumentTx();
    try {

      return db.command(query).execute(params);
    } finally {
      db.close();
    }
  }

  private ODatabaseDocumentTx getDatabaseDocumentTx() {
    return ODatabaseDocumentPool.global().acquire(getUrl(), username, password);
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    throw new NoSuchServiceConfigurationException(getName());

  }
}
