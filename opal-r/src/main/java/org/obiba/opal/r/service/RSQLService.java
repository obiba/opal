/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import org.apache.shiro.SecurityUtils;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.core.service.DataExportService;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.service.SQLException;
import org.obiba.opal.core.service.SQLService;
import org.obiba.opal.r.magma.MagmaAssignROperation;
import org.obiba.opal.r.service.event.RServiceInitializedEvent;
import org.obiba.opal.spi.r.*;
import org.obiba.opal.sql.SQLExtractor;
import org.obiba.opal.web.r.RPackageResourceHelper;
import org.obiba.opal.web.support.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.ws.rs.ForbiddenException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

@Component
public class RSQLService implements Service, SQLService {

  private static final Logger log = LoggerFactory.getLogger(RSQLService.class);

  private static final String R_WORK_DIR = System.getenv().get("OPAL_HOME") + File.separatorChar + "work" + File.separatorChar + "R";

  private static final String EXECUTE_SQL_FUNC = ".execute.SQL";

  private static final String EXECUTE_SQL_SCRIPT = EXECUTE_SQL_FUNC + ".R";

  @Autowired
  private RPackageResourceHelper rPackageHelper;

  @Autowired
  private RServerManagerService rServerManagerService;

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @Autowired
  protected IdentifiersTableService identifiersTableService;

  @Autowired
  protected DataExportService dataExportService;

  private boolean running = false;

  private boolean ensureSqldfDone;

  private Map<String, String> userRSessions = Maps.newHashMap();

  @Override
  public File executeToJSON(String datasource, String query, String idName) {
    if (!running) return null;

    RServerSession rSession = prepareRSession();

    try {
      String queryStr = prepareEnvironment(datasource, query, idName, rSession);

      // execute SQL
      String rOutput = "out.json";
      RScriptROperation rop = new RScriptROperation(String.format("%s.JSON('%s', '%s')", EXECUTE_SQL_FUNC, queryStr, rOutput), false);
      rSession.execute(rop);

      File output = new File(R_WORK_DIR, rSession.getId() + "-" + rOutput);
      FileReadROperation frop = new FileReadROperation(rOutput, output);
      rSession.execute(frop);
      return output;
    } catch (RRuntimeException e) {
      throw new SQLException(e);
    } finally {
      closeRSession(rSession.getUser());
    }
  }

  @Override
  public File executeToCSV(String datasource, String query, String idName) {
    if (!running) return null;

    RServerSession rSession = prepareRSession();

    try {
      String queryStr = prepareEnvironment(datasource, query, idName, rSession);

      // execute SQL
      String rOutput = "out.csv";
      RScriptROperation rop = new RScriptROperation(String.format("%s.CSV('%s', '%s')", EXECUTE_SQL_FUNC, queryStr, rOutput), false);
      rSession.execute(rop);

      File output = new File(R_WORK_DIR, rSession.getId() + "-" + rOutput);
      FileReadROperation frop = new FileReadROperation(rOutput, output);
      rSession.execute(frop);
      return output;
    } catch (RRuntimeException e) {
      throw new SQLException(e);
    }  finally {
      closeRSession(rSession.getUser());
    }
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    running = true;
    ensureSqldfDone = false;
  }

  @Override
  public void stop() {
    running = false;
    userRSessions.values().forEach(rSessionId -> opalRSessionManager.removeRSession(rSessionId));
    userRSessions.clear();
  }

  @Override
  public String getName() {
    return "r-sql";
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    throw new NoSuchServiceConfigurationException(getName());
  }

  //
  // Private methods
  //

  private RServerSession prepareRSession() {
    String user = getSubjectPrincipal();
    RServerSession rSession = null;
    if (userRSessions.containsKey(user))
      try {
        rSession = opalRSessionManager.getRSession(userRSessions.get(user));
      } catch (NoSuchRSessionException e) {
        // ignore
      }
    if (rSession != null) closeRSession(user);

    // start new R session
    rSession = opalRSessionManager.newSubjectRSession();
    rSession.setExecutionContext("SQL");
    userRSessions.put(user, rSession.getId());
    return rSession;
  }

  private String prepareEnvironment(String datasource, String query, String idName, RServerSession rSession) {
    // load utility functions
    SQLExecutorROperation fop = new SQLExecutorROperation();
    rSession.execute(fop);

    // assign tables
    Set<String> tables = SQLExtractor.extractTables(query);
    Datasource ds = MagmaEngine.get().getDatasource(datasource);
    for (String table : tables) {
      String tableName = extractTableName(table);
      if (!ds.hasValueTable(tableName))
        throw new NoSuchValueTableException(datasource, tableName);
      else
        ensureTableValuesAccess(datasource, tableName);
    }
    String queryStr = query;
    for (String table : tables) {
      String tableName = extractTableName(table);
      String tableSymbol = normalizeTableSymbol(tableName);
      if (!table.equals(tableSymbol))
        queryStr = queryStr.replaceAll(table, tableSymbol);
      MagmaAssignROperation mop = new MagmaAssignROperation(tableSymbol, datasource + "." + tableName, null, true,
          Strings.isNullOrEmpty(idName) ? "_id" : idName, null,
          MagmaAssignROperation.RClass.DATA_FRAME, identifiersTableService, dataExportService);
      rSession.execute(mop);
    }
    queryStr = queryStr.replaceAll("'", "\\\\'");
    log.info("SQL query: {}", queryStr);
    return queryStr;
  }

  private String extractTableName(String table) {
    if (table.startsWith("`") && table.endsWith("`"))
      return table.replaceAll("`", "");
    return table;
  }

  private String normalizeTableSymbol(String tableName) {
    return tableName.replaceAll("\\.", "_");
  }

  private void closeRSession(String user) {
    if (!userRSessions.containsKey(user)) return;
    opalRSessionManager.removeRSession(userRSessions.get(user));
    userRSessions.remove(user);
  }

  @Subscribe
  public void onRServiceStarted(RServiceInitializedEvent event) {
    finalizeServiceStart();
  }

  /**
   * Ensure sqldf R package is installed only once after the service has been started and R server is ready.
   */
  private void finalizeServiceStart() {
    if (!ensureSqldfDone) {
      try {
        rPackageHelper.ensureCRANPackage(rServerManagerService.getDefaultRServer(), "sqldf");
        ensureSqldfDone = true;
      } catch (Exception e) {
        log.error("Cannot ensure sqldf R package is installed", e);
      }
    }
  }

  private String getSubjectPrincipal() {
    if (!SecurityUtils.getSubject().isAuthenticated()) throw new ForbiddenException();
    return SecurityUtils.getSubject().getPrincipal().toString();
  }

  private void ensureTableValuesAccess(String datasource, String table) {
    if (!SecurityUtils.getSubject().isPermitted(String.format("rest:/datasource/%s/table/%s/valueSet:GET:GET/GET", datasource, table))) {
      throw new InvalidRequestException("AccessDeniedToTableValues", table);
    }
  }

  private static class SQLExecutorROperation extends AbstractROperation {

    private SQLExecutorROperation() {
    }

    @Override
    protected void doWithConnection() {
      try {
        try (InputStream is = new ClassPathResource(EXECUTE_SQL_SCRIPT).getInputStream();) {
          writeFile(EXECUTE_SQL_SCRIPT, is);
          eval(String.format("base::source('%s')", EXECUTE_SQL_SCRIPT));
        } catch (IOException e) {
          throw new RRuntimeException(e);
        }
      } catch (RServerException e) {
        log.warn("Failed preparing SQL executor from file '{}'", EXECUTE_SQL_SCRIPT, e);
        throw new RRuntimeException(e);
      }
    }
  }
}
