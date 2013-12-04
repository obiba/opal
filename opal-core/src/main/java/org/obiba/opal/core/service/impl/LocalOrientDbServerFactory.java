package org.obiba.opal.core.service.impl;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;

import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.service.OrientDbServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

  private OServer server;

  private OpalConfigurationService opalConfigurationService;

  private String password;

  @Autowired
  public void setOpalConfigurationService(OpalConfigurationService opalConfigurationService) {
    this.opalConfigurationService = opalConfigurationService;
  }

  @Value(URL)
  @Override
  public void setUrl(@NotNull String url) {
    this.url = url;
  }

  @PostConstruct
  public void start() throws Exception {
    log.info("Start OrientDB server ({})", url);
    System.setProperty("ORIENTDB_HOME", ORIENTDB_HOME);

    server = new OServer() //
        .startup(LocalOrientDbServerFactory.class.getResourceAsStream("/orientdb-server-config.xml")) //
        .activate();

    ODatabaseDocumentTx database = new ODatabaseDocumentTx(url);
    if(!database.exists()) {
      database.create();
    }
    database.close();
  }

  @PreDestroy
  public void stop() {
    log.info("Stop OrientDB server ({})", url);
    if(server != null) server.shutdown();
  }

  @NotNull
  @Override
  public OServer getServer() {
    return server;
  }

  @NotNull
  @Override
  public ODatabaseDocumentTx getDocumentTx() {
    log.info("Open connection with {} / {}", USERNAME, getPassword());
    return ODatabaseDocumentPool.global().acquire(url, USERNAME, getPassword());
  }

  private String getPassword() {
    if(password == null) {
      password = opalConfigurationService.getOpalConfiguration().getDatabasePassword();
    }
    return password;
  }

}
