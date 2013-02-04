/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.upgrade.binary;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.managed.BasicManagedDataSource;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.runtime.BackgroundJob;
import org.obiba.opal.core.runtime.jdbc.DataSourceFactory;
import org.obiba.opal.core.runtime.jdbc.DefaultJdbcDataSourceRegistry;
import org.obiba.opal.core.runtime.jdbc.JdbcDataSource;
import org.obiba.opal.core.runtime.support.OpalConfigurationProvider;
import org.obiba.opal.core.support.TimedExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;

@SuppressWarnings("MagicNumber")
@Component
public class BinariesStorageUpgradeJob implements BackgroundJob {

  private static final Logger log = LoggerFactory.getLogger(BinariesStorageUpgradeJob.class);

  private String progressStatus;

  private int progress;

  private final Map<DataSource, String> dataSourceNames = new LinkedHashMap<DataSource, String>();

  @Qualifier("opalConfigurationProvider")
  @Autowired
  private OpalConfigurationProvider opalConfigurationProvider;

  @Autowired
  private BasicManagedDataSource opalDataSource;

  @Autowired
  private BasicManagedDataSource keyDataSource;

  @Qualifier("dataSourceFactory")
  @Autowired
  private DataSourceFactory dataSourceFactory;

  @Autowired
  private BinaryMover binaryMover;

  @Override
  public void run() {
    dataSourceNames.put(opalDataSource, "Default");
    dataSourceNames.put(keyDataSource, "Key");

    OpalConfiguration configuration = opalConfigurationProvider.readOpalConfiguration(false);
    try {
      DefaultJdbcDataSourceRegistry.JdbcDataSourcesConfig dataSourcesConfig = configuration
          .getExtension(DefaultJdbcDataSourceRegistry.JdbcDataSourcesConfig.class);
      for(JdbcDataSource jdbcDataSource : dataSourcesConfig.getDatasources()) {
        dataSourceNames.put(dataSourceFactory.createDataSource(jdbcDataSource), jdbcDataSource.getName());
      }
    } catch(NoSuchElementException e) {
      // ignore
    }

    for(Map.Entry<DataSource, String> entry : dataSourceNames.entrySet()) {
      DataSource dataSource = entry.getKey();
      String dataSourceName = entry.getValue();
      log.debug("Process dataSource {}", dataSourceName);
      if(SqlBinariesStorageUpgradeStep.hasHibernateDatasource(dataSource)) {
        processDataSource(dataSource, dataSourceName);
      }
    }
  }

  @SuppressWarnings("OverlyNestedMethod")
  private void processDataSource(DataSource dataSource, String name) {
    TimedExecution timedExecution = new TimedExecution().start();
    Collection<BinaryToMove> binaries = findBinaries(dataSource);
    int nbBinaries = binaries.size();
    log.info("{} binaries to move for datasource {}", nbBinaries, name);
    for(BinaryToMove binary : binaries) {
      boolean isMagmaLoaded;
      do {
        try {
          isMagmaLoaded = true;
          binaryMover.move(binary);
          progress++;
        } catch(NoSuchValueTableException e) {
          log.debug("Magma not fully loaded. Wait & retry in 5s.", e);
          isMagmaLoaded = false;
          try {
            Thread.sleep(5000);
          } catch(InterruptedException ignored) {
          }
        }
      } while(!isMagmaLoaded);

      logProgress(name, nbBinaries);
    }

    log.info("Datasource {}: {} binaries moved in {}", name, nbBinaries, timedExecution.end().formatExecutionTime());
  }

  private void logProgress(String name, int nbBinaries) {
    if(progress % 10 == 0) {
      double percent = (double) progress / (double) nbBinaries * 100d;
      progressStatus = "Datasource " + name + ": " + String.format("%.1f", percent) + "% processed (" + progress + "/" +
          nbBinaries + ")";
      log.info(progressStatus);
    }
  }

  private Collection<BinaryToMove> findBinaries(DataSource dataSource) {
    JdbcTemplate template = new JdbcTemplate(dataSource);
    final Collection<BinaryToMove> binaries = new ArrayList<BinaryToMove>();
    template.query( //
        "SELECT d.name AS datasourceName, vt.name AS tableName, v.name AS variableName, ve.identifier AS entityId " + //
            "FROM value_set_value vsv, value_set vs, variable_entity ve, value_table vt, datasource d, variable v  " +
            //
            "WHERE vsv.value_set_id = vs.id " + //
            "AND vs.variable_entity_id = ve.id " + //
            "AND vs.value_table_id = vt.id " + //
            "AND vt.datasource_id = d.id " + //
            "AND v.id = vsv.variable_id " + //
            "AND v.value_table_id = vt.id " + //
            "AND vsv.value_type = ?", new Object[] { "binary" }, new RowCallbackHandler() {

      @Override
      public void processRow(ResultSet rs) throws SQLException, NoSuchValueTableException {
        binaries.add(
            new BinaryToMove(rs.getString("datasourceName"), rs.getString("tableName"), rs.getString("variableName"),
                rs.getString("entityId")));
      }
    });
    return binaries;
  }

  @Override
  public String getName() {
    return "BinariesStorageUpgrade";
  }

  @Override
  public String getDescription() {
    return "Moves the binary values from an base64 encoded string to a blob in Hibernate Datasources.";
  }

  @Override
  public int getPriority() {
    return Thread.MIN_PRIORITY;
  }

  @Override
  public int getProgress() {
    return progress;
  }

  @Override
  public String getProgressStatus() {
    return progressStatus;
  }

}