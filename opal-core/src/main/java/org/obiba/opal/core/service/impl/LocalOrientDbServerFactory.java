package org.obiba.opal.core.service.impl;

import javax.annotation.Nonnull;

import org.obiba.opal.core.service.OrientDbServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.object.db.OObjectDatabasePool;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import com.orientechnologies.orient.server.OServer;

@Component
public class LocalOrientDbServerFactory implements OrientDbServerFactory {

  private static final Logger log = LoggerFactory.getLogger(LocalOrientDbServerFactory.class);

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

  @Nonnull
  @Override
  public OServer getServer() {
    return server;
  }

  @Nonnull
  @Override
  public OObjectDatabaseTx getDatabaseDocumentTx() {
    return OObjectDatabasePool.global().acquire(url, username, password);
  }

}
