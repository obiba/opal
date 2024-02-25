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

import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.server.OServer;
import javax.validation.constraints.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class LocalOrientDbServerFactory implements OrientDbServerFactory, InitializingBean, DisposableBean {

  private static final Logger log = LoggerFactory.getLogger(LocalOrientDbServerFactory.class);

  private static final String ORIENTDB_HOME = "${OPAL_HOME}/data/orientdb/opal-config";

  public static final String DEFAULT_SCHEME = "plocal";

  public static final String USERNAME = "admin";

  public static final String PASSWORD = "admin";

  public static final String URL = DEFAULT_SCHEME + ":" + ORIENTDB_HOME;

  private String url;

  private OServer server;

  private OPartitionedDatabasePoolFactory poolFactory;

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

  @Override
  public void afterPropertiesSet() throws Exception {
    start();
  }

  @Override
  public void start() throws Exception {
    stop();
    log.info("Start OrientDB server ({})", url);

    System.setProperty("ORIENTDB_HOME", url.replaceFirst("^" + DEFAULT_SCHEME + ":", ""));
    System.setProperty("ORIENTDB_ROOT_PASSWORD", PASSWORD);
    System.setProperty("java.util.logging.manager", "com.orientechnologies.common.log.OLogManager$DebugLogManager");

    ensureSecurityConfig();

    server = new OServer() //
        .startup(LocalOrientDbServerFactory.class.getResourceAsStream("/orientdb-server-config.xml")) //
        .activate();
    poolFactory = new OPartitionedDatabasePoolFactory();

    ensureDatabaseExists();
  }

  @Override
  public void destroy() {
    stop();
  }

  @Override
  public void stop() {
    if (server != null) {
      log.info("Stop OrientDB server ({})", url);
      server.shutdown();
      server = null;
    }
  }

  @NotNull
  @Override
  public OServer getServer() {
    return server;
  }

  @NotNull
  @Override
  public ODatabaseDocument getDocumentTx() {
    //TODO cache password
//    String password = opalConfigurationService.getOpalConfiguration().getDatabasePassword();
    log.trace("Open OrientDB connection with username: {}", USERNAME);
    return poolFactory.get(url, USERNAME, PASSWORD).acquire();
  }

  private void ensureSecurityConfig() throws IOException {
    File securityFile = new File(System.getProperty("ORIENTDB_HOME") + File.separator + "config", "security.json");
    if (!securityFile.exists()) {
      if (!securityFile.getParentFile().exists()) {
        securityFile.getParentFile().mkdirs();
      }
      securityFile.createNewFile();
      JSONObject securityObject = new JSONObject();
      securityObject.put("enabled", false);
      try (PrintWriter out = new PrintWriter(securityFile.getAbsolutePath())) {
        out.println(securityObject);
      }
    }
  }

  private void ensureDatabaseExists() {
    try(ODatabaseDocument database = new ODatabaseDocumentTx(url)) {
      if(!database.exists()) {
        database.create();
      }
    }
  }
}
