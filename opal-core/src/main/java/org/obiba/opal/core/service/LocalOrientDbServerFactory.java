package org.obiba.opal.core.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.server.OServer;

@Component
public class LocalOrientDbServerFactory implements OrientDbServerFactory {

  private static final Logger log = LoggerFactory.getLogger(LocalOrientDbServerFactory.class);

  private static final String ORIENTDB_HOME = "${OPAL_HOME}/data/orientdb/opal-config";

  public static final String DEFAULT_SCHEME = "plocal";

  public static final String USERNAME = "admin";

  public static final String PASSWORD = "admin";

  public static final String URL = DEFAULT_SCHEME + ":" + ORIENTDB_HOME;

  private String url;

  private OServer server;

  // TODO: wait for this issue to be fixed to change admin password
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

  @PostConstruct
  public void start() throws Exception {
    log.info("Start OrientDB server ({})", url);

    System.setProperty("ORIENTDB_HOME", url.replaceFirst("^" + DEFAULT_SCHEME + ":", ""));
    System.setProperty("ORIENTDB_ROOT_PASSWORD", PASSWORD);

    server = new OServer() //
        .startup(LocalOrientDbServerFactory.class.getResourceAsStream("/orientdb-server-config.xml")) //
        .activate();

    ensureDatabaseExists();
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
    //TODO cache password
//    String password = opalConfigurationService.getOpalConfiguration().getDatabasePassword();
//    log.info("Open OrientDB connection with {} / {}", USERNAME, password);
//    return ODatabaseDocumentPool.global().acquire(url, USERNAME, password);
    ODatabaseDocumentTx db = ODatabaseDocumentPool.global().acquire(url, USERNAME, PASSWORD);
    ODatabaseRecordThreadLocal.INSTANCE.set(db);

    return db;
  }

  private void ensureDatabaseExists() {
    try(ODatabaseDocumentTx database = new ODatabaseDocumentTx(url)) {
      if(!database.exists()) {
        database.create();
      }
    }
  }
}
