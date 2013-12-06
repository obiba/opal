package org.obiba.opal.core.service.impl;

import javax.validation.constraints.NotNull;

import org.obiba.opal.core.service.OrientDbServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.server.OServer;

@Component
public class LocalOrientDbServerFactory implements OrientDbServerFactory {

  private static final Logger log = LoggerFactory.getLogger(LocalOrientDbServerFactory.class);

  private static final String ORIENTDB_HOME = "${OPAL_HOME}/data/orientdb/opal-config";

  public static final String URL = "local:" + ORIENTDB_HOME;

  public static final String USERNAME = "admin";

  private String url;

  private static OServer server;

  // TODO: wait for these issues to be fixed to start OrientDB with @PostConstruct and change admin password
  // https://github.com/orientechnologies/orientdb/issues/1876
  // https://github.com/orientechnologies/orientdb/pull/1870

//  private OpalConfigurationService opalConfigurationService;
//
//  @Autowired
//  public void setOpalConfigurationService(OpalConfigurationService opalConfigurationService) {
//    this.opalConfigurationService = opalConfigurationService;
//  }

  @Value(URL)
  @Override
  public void setUrl(@NotNull String url) {
    this.url = url;
  }

  public static void start(String url) {
    log.info("Start OrientDB server ({})", url);
    System.setProperty("ORIENTDB_HOME", ORIENTDB_HOME);
    try {
      server = new OServer() //
          .startup(LocalOrientDbServerFactory.class.getResourceAsStream("/orientdb-server-config.xml")) //
          .activate();

      // create database if does not exist
      ODatabase database = new ODatabaseDocumentTx(url);
      if(!database.exists()) {
        database.create();
      }
      database.close();

    } catch(Exception e) {
      log.error("Cannot start OrientDB server", e);
      throw new RuntimeException("Cannot start OrientDB server", e);
    }
  }

//  @PostConstruct
//  public void start() throws Exception {
//    log.info("Start OrientDB server ({})", url);
//    System.setProperty("ORIENTDB_HOME", ORIENTDB_HOME);
//    server = new OServer() //
//        .startup(LocalOrientDbServerFactory.class.getResourceAsStream("/orientdb-server-config.xml")) //
//        .activate();
//    ODatabaseDocumentTx database = new ODatabaseDocumentTx(url);
//    if(!database.exists()) database.create();
//    database.close();
//  }

  public static void stop() {
    if(server != null) server.shutdown();
  }

//  @PreDestroy
//  public void stop() {
//    log.info("Stop OrientDB server ({})", url);
//    if(server != null) server.shutdown();
//  }

  @NotNull
  @Override
  public OServer getServer() {
    return server;
  }

  @NotNull
  @Override
  public ODatabaseDocumentTx getDocumentTx() {
    //TODO cache password
//    String password = opalConfigurationService.getOpalConfiguration().getDatabasePassword();
//    log.info("Open OrientDB connection with {} / {}", USERNAME, password);
//    return ODatabaseDocumentPool.global().acquire(url, USERNAME, password);
    return ODatabaseDocumentPool.global().acquire(url, USERNAME, USERNAME);
  }

}
