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
import org.json.JSONObject;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.domain.sql.SQLExecution;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.core.service.*;
import org.obiba.opal.r.magma.MagmaAssignROperation;
import org.obiba.opal.r.service.event.RServiceInitializedEvent;
import org.obiba.opal.spi.r.*;
import org.obiba.opal.sql.SQLExtractor;
import org.obiba.opal.sql.SQLParserException;
import org.obiba.opal.web.support.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.ForbiddenException;
import java.io.*;
import java.text.Normalizer;
import java.util.*;

@Component
public class RSQLService implements Service, SQLService {

  private static final Logger log = LoggerFactory.getLogger(RSQLService.class);

  private static final String R_WORK_DIR = System.getenv().get("OPAL_HOME") + File.separatorChar + "work" + File.separatorChar + "R";

  private static final String EXECUTE_SQL_FUNC = ".execute.SQL";

  private static final String EXECUTE_SQL_SCRIPT = EXECUTE_SQL_FUNC + ".R";

  private static final int SQLITE_MAX_COLUMN = 2000;

  private final SystemLogService systemLogService;

  private final RServerManagerService rServerManagerService;

  private final OpalRSessionManager opalRSessionManager;

  protected final IdentifiersTableService identifiersTableService;

  protected final DataExportService dataExportService;

  protected final RCacheHelper rCacheHelper;

  private boolean running = false;

  private boolean ensureSqldfDone;

  private List<String> userRSessions = Collections.synchronizedList(Lists.newArrayList());

  @Autowired
  public RSQLService(SystemLogService systemLogService, RServerManagerService rServerManagerService, OpalRSessionManager opalRSessionManager, IdentifiersTableService identifiersTableService, DataExportService dataExportService, RCacheHelper rCacheHelper) {
    this.systemLogService = systemLogService;
    this.rServerManagerService = rServerManagerService;
    this.opalRSessionManager = opalRSessionManager;
    this.identifiersTableService = identifiersTableService;
    this.dataExportService = dataExportService;
    this.rCacheHelper = rCacheHelper;
  }

  @Override
  public File execute(@Nullable String datasource, String query, String idName, Output output) {
    if (!running) return null;

    SQLExecution sqlExec = new SQLExecution();
    sqlExec.setDatasource(datasource);
    sqlExec.setQuery(query);
    sqlExec.setUser(getSubjectPrincipal());

    RServerSession rSession = prepareRSession();

    try {
      String queryStr = prepareEnvironment(datasource, query, idName, rSession);

      // execute SQL
      String rOutput = "out." + output.toString().toLowerCase();
      RScriptROperation rop = new RScriptROperation(String.format("%s.%s('%s', '%s')", EXECUTE_SQL_FUNC, output.toString(), queryStr, rOutput), false);
      rSession.execute(rop);

      File outputFile = new File(R_WORK_DIR, rSession.getId() + "-" + rOutput);
      FileReadROperation frop = new FileReadROperation(rOutput, outputFile);
      rSession.execute(frop);
      return outputFile;
    } catch (RRuntimeException | SQLParserException e) {
      sqlExec.setError(e.getMessage());
      throw new SQLException(e);
    } catch (Exception e) {
      sqlExec.setError(e.getMessage());
      throw e;
    } finally {
      saveSQLExecutionHistory(sqlExec);
      closeRSession(rSession.getId());
    }
  }

  @Override
  public List<SQLExecution> getSQLExecutions(String subject, String datasource) {
    List<SQLExecution> sqlExecs = Lists.newArrayList();
    try (BufferedReader br = new BufferedReader(new FileReader(systemLogService.getSQLLogFile()))) {
      for (String line; (line = br.readLine()) != null; ) {
        JSONObject exec = new JSONObject(line);
        boolean included = true;
        if (!Strings.isNullOrEmpty(subject) && !"*".equals(subject))
          included = exec.getString("user").equals(subject);
        if (!Strings.isNullOrEmpty(datasource)) {
          if ("*".equals(datasource))
            included = included && !exec.has("datasource");
          else
            included = included && (exec.has("datasource") && datasource.equals(exec.getString("datasource")));
        }
        if (included) {
          SQLExecution sqlExec = new SQLExecution();
          sqlExec.setUser(exec.getString("user"));
          sqlExec.setDatasource(exec.has("datasource") ? exec.getString("datasource") : null);
          sqlExec.setError(exec.has("error") ? exec.getString("error") : null);
          sqlExec.setQuery(exec.getString("query"));
          sqlExec.setEnded(exec.getLong("ended"));
          sqlExec.setStarted(exec.getLong("started"));
          sqlExecs.add(sqlExec);
        }
      }
    } catch (IOException e) {
      log.error("Cannot read SQL log file: {}", systemLogService.getSQLLogFile(), e);
    }
    sqlExecs.sort((exec1, exec2) -> (int) (exec2.getEnded() - exec1.getEnded()));
    return sqlExecs;
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

  private synchronized void saveSQLExecutionHistory(SQLExecution sqlExec) {
    try {
      sqlExec.setEnded(new Date().getTime());
      SQLLog.log("{}", new JSONObject(sqlExec));
    } catch (Exception e) {
      log.error("Cannot save SQL execution history entry", e);
    }
  }

  private RServerSession prepareRSession() {
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
    Map<String, ValueTable> fromTableFullNameMap = Maps.newHashMap();
    Datasource ds = Strings.isNullOrEmpty(datasource) ? null : MagmaEngine.get().getDatasource(datasource);
    fromTables.forEach(fromTable -> fromTableFullNameMap.put(fromTable, extractValueTable(ds, fromTable)));

    String queryStr = query;
    for (String fromTable : fromTables) {
      ValueTable valueTable = fromTableFullNameMap.get(fromTable);
      if (valueTable.getVariableCount() >= SQLITE_MAX_COLUMN)
        throw new SQLException("Table " + fromTable + " has too much variables (limit is " + (SQLITE_MAX_COLUMN - 1) + ")");
      String tableSymbol = normalizeTableSymbol(fromTable);
      if (!fromTable.equals(tableSymbol))
        queryStr = queryStr.replaceAll(fromTable, tableSymbol);
      MagmaAssignROperation mop = new MagmaAssignROperation(tableSymbol, valueTable,
          dataExportService, rCacheHelper,
          Strings.isNullOrEmpty(idName) ? DEFAULT_ID_COLUMN : idName,
          MagmaAssignROperation.RClass.DATA_FRAME_NO_FACTORS);
      rSession.execute(mop);
    }
    queryStr = queryStr.replaceAll("'", "\\\\'");
    log.debug("SQL query to execute: {}", queryStr);
    return queryStr;
  }

  private ValueTable extractValueTable(Datasource datasource, String table) {
    String tableName = extractTableName(table);
    ValueTable valueTable = null;

    // when no datasource context, FROM table names must be fully qualified
    if (datasource == null) {
      MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
      // can throw datasource/table not found exceptions
      valueTable = resolver.resolveTable();
      ensureTableValuesAccess(resolver.getDatasourceName(), resolver.getTableName());
    } else if (datasource.hasValueTable(tableName)) {
      // if there is a datasource context, FROM table names can be relative to it
      ensureTableValuesAccess(datasource.getName(), tableName);
      valueTable = datasource.getValueTable(tableName);
    } else if (tableName.contains(".")) {
      // second chance: try it as a fully qualified table name
      MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
      // can throw datasource/table not found exceptions
      valueTable = resolver.resolveTable();
      ensureTableValuesAccess(resolver.getDatasourceName(), resolver.getTableName());
    } else
      throw new NoSuchValueTableException(datasource.getName(), tableName);

    if (valueTable.getValueSetCount() == 0)
      throw new SQLException("Table " + tableName + " has no data.");
    return valueTable;
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
    String fromTableN = Normalizer.normalize(extractTableName(fromTable), Normalizer.Form.NFKD);
    return fromTableN.replaceAll("[^A-Za-z0-9]", "_");
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
        rServerManagerService.getDefaultRServer().ensureCRANPackage("sqldf");
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
