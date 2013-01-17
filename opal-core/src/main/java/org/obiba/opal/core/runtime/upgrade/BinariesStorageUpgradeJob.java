/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.upgrade;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.hibernate.HibernateDatasource;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.runtime.BackgroundJob;
import org.obiba.opal.core.runtime.jdbc.DataSourceFactory;
import org.obiba.opal.core.runtime.jdbc.DefaultJdbcDataSourceRegistry;
import org.obiba.opal.core.runtime.jdbc.JdbcDataSource;
import org.obiba.opal.core.runtime.support.OpalConfigurationProvider;
import org.obiba.opal.core.support.TimedExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;

@Component
public class BinariesStorageUpgradeJob implements BackgroundJob {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private int progress;

  private String progressStatus;

  private final Map<DataSource, String> dataSourceNames = new LinkedHashMap<DataSource, String>();

  private OpalConfigurationProvider opalConfigurationProvider;

  private DataSource opalDataSource;

  private DataSource keyDataSource;

  private DataSourceFactory dataSourceFactory;

  @Override
  public void run() {
    dataSourceNames.put(opalDataSource, "Default");
    dataSourceNames.put(keyDataSource, "Key");

    OpalConfiguration configuration = opalConfigurationProvider.readOpalConfiguration();
    DefaultJdbcDataSourceRegistry.JdbcDataSourcesConfig dataSourcesConfig = configuration
        .getExtension(DefaultJdbcDataSourceRegistry.JdbcDataSourcesConfig.class);
    for(JdbcDataSource jdbcDataSource : dataSourcesConfig.getDatasources()) {
      dataSourceNames.put(dataSourceFactory.createDataSource(jdbcDataSource), jdbcDataSource.getName());
    }

    for(Map.Entry<DataSource, String> entry : dataSourceNames.entrySet()) {
      DataSource dataSource = entry.getKey();
      String dataSourceName = entry.getValue();
      log.debug("Process dataSource {}", dataSourceName);
      if(SqlBinariesStorageUpgradeStep.hasHibernateDatasource(dataSource)) {
        process(dataSource, dataSourceName);
      }
    }

  }

  private void process(DataSource dataSource, String name) {
    TimedExecution timedExecution = new TimedExecution().start();
    JdbcTemplate template = new JdbcTemplate(dataSource);
    int nbBinaries = template.queryForInt("SELECT COUNT(created) FROM value_set_value WHERE value_type = ?", "binary");
    log.info("{} binaries to move for datasource {}", nbBinaries, name);
    if(nbBinaries > 0) {
      for(int i = 0; i < nbBinaries; i++) {

        template.query( //
            "SELECT d.name AS datasourceName, vt.name AS tableName, v.name AS variableName " + //
                "FROM value_set_value vsv, value_set vs, variable_entity ve, value_table vt, datasource d, variable v  " + //
                "WHERE vsv.value_set_id = vs.id " + //
                "AND vs.variable_entity_id = ve.id " + //
                "AND vs.value_table_id = vt.id " + //
                "AND vt.datasource_id = d.id " + //
                "AND v.id = vsv.variable_id " + //
                "AND vsv.value_type = ? " + //
                "LIMIT 1", new Object[] { "binary" }, new RowCallbackHandler() {

          @Override
          public void processRow(ResultSet rs) throws SQLException, NoSuchValueTableException {

            boolean isMagmaLoaded;
            do {
              try {
                isMagmaLoaded = true;
                Datasource datasource = MagmaEngine.get().getDatasource(rs.getString("datasourceName"));
                ValueTable table = datasource.getValueTable(rs.getString("tableName"));
                VariableEntity entity = new VariableEntityBean(table.getEntityType(), rs.getString("entityId"));
                ValueSet valueSet = table.getValueSet(entity);
                Variable variable = table.getVariable(rs.getString("variableName"));
                Value value = table.getValue(variable, valueSet);
                ValueTableWriter tableWriter = null;
                ValueTableWriter.ValueSetWriter valueSetWriter = null;
                try {
                  tableWriter = datasource.createWriter(table.getName(), table.getEntityType());
                  valueSetWriter = tableWriter.writeValueSet(entity);
                  valueSetWriter.writeValue(variable, value);
                } finally {
                  if(valueSetWriter != null) {
                    try {
                      valueSetWriter.close();
                    } catch(IOException ignored) {
                    }
                  }
                  if(tableWriter != null) {
                    try {
                      tableWriter.close();
                    } catch(IOException ignored) {
                    }
                  }
                }

              } catch(NoSuchValueTableException e) {
                // Magma not fully loaded
                isMagmaLoaded = false;
                try {
                  wait(5000);
                } catch(InterruptedException ignored) {
                }
              }

            } while(!isMagmaLoaded);
          }
        }

        );

      }
    }
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
    return 0;
  }

  @Override
  public String getProgressStatus() {
    return progressStatus;
  }

  public void setOpalConfigurationProvider(OpalConfigurationProvider opalConfigurationProvider) {
    this.opalConfigurationProvider = opalConfigurationProvider;
  }

  public void setOpalDataSource(DataSource opalDataSource) {
    this.opalDataSource = opalDataSource;
  }

  public void setKeyDataSource(DataSource keyDataSource) {
    this.keyDataSource = keyDataSource;
  }

  public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
    this.dataSourceFactory = dataSourceFactory;
  }
}