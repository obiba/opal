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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import org.apache.shiro.SecurityUtils;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.support.MagmaEngineTableResolver;
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
import org.obiba.opal.sql.SQLParserException;
import org.obiba.opal.web.r.RPackageResourceHelper;
import org.obiba.opal.web.support.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.ws.rs.ForbiddenException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
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

  private List<String> userRSessions = Collections.synchronizedList(Lists.newArrayList());

  @Override
  public File executeToJSON(@Nullable String datasource, String query, String idName) {
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
    } catch (RRuntimeException | SQLParserException e) {
      throw new SQLException(e);
    } finally {
      closeRSession(rSession.getId());
    }
  }

  @Override
  public File executeToCSV(@Nullable String datasource, String query, String idName) {
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
    } catch (RRuntimeException | SQLParserException e) {
      throw new SQLException(e);
    } finally {
      closeRSession(rSession.getId());
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
    userRSessions.forEach(rSessionId -> opalRSessionManager.removeRSession(rSessionId));
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
    RServerSession rSession = opalRSessionManager.newSubjectRSession();
    rSession.setExecutionContext("SQL");
    userRSessions.add(rSession.getId());
    return rSession;
  }

  private String prepareEnvironment(@Nullable String datasource, String query, String idName, RServerSession rSession) throws SQLParserException {
    // load utility functions
    SQLExecutorROperation fop = new SQLExecutorROperation();
    rSession.execute(fop);

    // validate tables exist and their data are accessible
    Set<String> fromTables = SQLExtractor.extractTables(query);
    Map<String, String> fromTableFullNameMap = Maps.newHashMap();
    Datasource ds = Strings.isNullOrEmpty(datasource) ? null : MagmaEngine.get().getDatasource(datasource);
    fromTables.forEach(fromTable -> fromTableFullNameMap.put(fromTable, extractFullTableName(ds, fromTable)));

    String queryStr = query;
    for (String fromTable : fromTables) {
      String tableFullName = fromTableFullNameMap.get(fromTable);
      String tableSymbol = normalizeTableSymbol(fromTable);
      if (!fromTable.equals(tableSymbol))
        queryStr = queryStr.replaceAll(fromTable, tableSymbol);
      MagmaAssignROperation mop = new MagmaAssignROperation(tableSymbol, tableFullName, null, true,
          Strings.isNullOrEmpty(idName) ? DEFAULT_ID_COLUMN : idName, null,
          MagmaAssignROperation.RClass.DATA_FRAME, identifiersTableService, dataExportService);
      rSession.execute(mop);
    }
    queryStr = queryStr.replaceAll("'", "\\\\'");
    log.info("SQL query: {}", queryStr);
    return queryStr;
  }

  private String extractFullTableName(Datasource datasource, String table) {
    String tableName = extractTableName(table);

    // when no datasource context, FROM table names are fully qualified
    if (datasource == null) {
      MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
      // can throw datasource/table not found exceptions
      resolver.resolveTable();
      ensureTableValuesAccess(resolver.getDatasourceName(), resolver.getTableName());
      return tableName;
    }

    // if there is a datasource context, FROM table names are relative to it
    if (!datasource.hasValueTable(tableName))
      throw new NoSuchValueTableException(datasource.getName(), tableName);
    else
      ensureTableValuesAccess(datasource.getName(), tableName);
    return datasource.getName() + "." + tableName;
  }

  /**
   * Remove escape chars from FROM statement.
   *
   * @param fromTable
   * @return
   */
  private String extractTableName(String fromTable) {
    if (fromTable.startsWith("`") && fromTable.endsWith("`"))
      return fromTable.replaceAll("`", "");
    return fromTable;
  }

  /**
   * Get the R symbol to assign from the FROM statement.
   *
   * @param fromTable
   * @return
   */
  private String normalizeTableSymbol(String fromTable) {
    return extractTableName(fromTable).replaceAll("\\.", "_");
  }

  private void closeRSession(String rSessionId) {
    opalRSessionManager.removeRSession(rSessionId);
    userRSessions.remove(rSessionId);
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
