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

import com.google.common.base.Strings;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.server.OServer;
import javax.validation.constraints.NotNull;
import org.json.JSONObject;
import org.obiba.core.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class LocalOrientDbServerFactory implements OrientDbServerFactory, InitializingBean, DisposableBean {

  private static final Logger log = LoggerFactory.getLogger(LocalOrientDbServerFactory.class);

  private static final String ORIENTDB_HOME = "${OPAL_HOME}/data/orientdb/opal-config";

  public static final String DEFAULT_SCHEME = "plocal";

  public static final String USERNAME = "admin";

  public static final String PASSWORD = "admin";

  public static final String URL = DEFAULT_SCHEME + ":" + ORIENTDB_HOME;

  private final Lock lock = new ReentrantLock();
  private final Condition condition = lock.newCondition();

  private boolean started = false;

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
    String orientDBHome = url.replaceFirst("^" + DEFAULT_SCHEME + ":", "");
    System.setProperty("ORIENTDB_HOME", orientDBHome);
    System.setProperty("ORIENTDB_ROOT_PASSWORD", PASSWORD);

    File osysFolder = new File(orientDBHome, "databases" + File.separator + "OSystem");
    if (osysFolder.exists()) {
      File osysFolderBackup = new File(orientDBHome, "databases" + File.separator + ".OSystem.bak");
      if (!osysFolderBackup.exists()) {
        FileUtil.copyDirectory(osysFolder, osysFolderBackup);
        FileUtil.delete(osysFolder);
      }
    }

    if (Strings.isNullOrEmpty(url)) {
      this.url = URL;
    }
    log.info("Start OrientDB server ({})", url);


    ensureSecurityConfig();

    server = new OServer() //
        .startup(LocalOrientDbServerFactory.class.getResourceAsStream("/orientdb-server-config.xml")) //
        .activate();
    poolFactory = new OPartitionedDatabasePoolFactory();

    ensureDatabaseExists();
    started = true;
    signalCondition();
  }

  @Override
  public void destroy() {
    stop();
  }

  @Override
  public void stop() {
    // Signal waiting threads first so they can wake up and fail gracefully
    lock.lock();
    try {
      started = false;
      condition.signalAll();
    } finally {
      lock.unlock();
    }
    if (poolFactory != null) {
      log.info("Close OrientDB connection pool ({})", url);
      poolFactory.close();
      poolFactory = null;
    }
    if (server != null) {
      log.info("Stop OrientDB server ({})", url);
      server.shutdown();
      server = null;
    }
    // OServer()'s default shutdownEngineOnExit=false means server.shutdown() does NOT
    // call Orient.instance().shutdown(), so OLocalPaginatedStorage.close() is never
    // reached, the WAL is not checkpointed, and dirty.fl files are left behind.
    // Calling it explicitly here guarantees every registered storage is properly closed.
    log.info("Shutdown OrientDB engine");
    Orient.instance().shutdown();
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
    try {
      waitForCondition();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("OrientDB server is not available (interrupted while waiting)", e);
    }
    OPartitionedDatabasePoolFactory factory = poolFactory;
    if (factory == null) {
      throw new IllegalStateException("OrientDB server is not available (pool has been closed)");
    }
    return factory.get(url, USERNAME, PASSWORD).acquire();
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

  public void waitForCondition() throws InterruptedException {
    lock.lock();
    try {
      while (!started) {
        condition.await();
        // If we were woken by stop(), started is still false — treat as interrupted
        if (!started) {
          throw new InterruptedException("OrientDB server was stopped while waiting for it to start");
        }
      }
    } finally {
      lock.unlock();
    }
  }

  public void signalCondition() {
    lock.lock(); // Acquire the lock
    try {
      // Update the condition and signal waiting threads
      started = true;
      condition.signalAll(); // Signal all waiting threads
    } finally {
      lock.unlock(); // Release the lock
    }
  }

}
