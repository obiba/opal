/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.support;

import javax.annotation.Nonnull;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.Disposable;
import org.obiba.magma.datasource.jdbc.JdbcDatasource;
import org.obiba.magma.datasource.jdbc.JdbcDatasourceSettings;
import org.obiba.magma.datasource.jdbc.JdbcValueTableSettings;
import org.obiba.opal.core.domain.database.SqlDatabase;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class DatabaseJdbcDatasourceFactory extends AbstractDatasourceFactory implements Disposable {

  private String databaseName;

  // transient because of XML serialization
  @SuppressWarnings("TransientFieldInNonSerializableClass")
  @Autowired
  private transient DatabaseRegistry databaseRegistry;

  // empty public constructor because of XML serialization
  @SuppressWarnings("UnusedDeclaration")
  public DatabaseJdbcDatasourceFactory() {
  }

  public DatabaseJdbcDatasourceFactory(String name, String databaseName, DatabaseRegistry databaseRegistry) {
    setName(name);
    this.databaseName = databaseName;
    this.databaseRegistry = databaseRegistry;
  }

  @Nonnull
  @Override
  protected Datasource internalCreate() {
    return new JdbcDatasource(getName(), databaseRegistry.getDataSource(databaseName, getName()), getSettings());
  }

  private JdbcDatasourceSettings getSettings() {
    SqlDatabase database = (SqlDatabase) databaseRegistry.getDatabase(databaseName);
    if(database == null) {
      throw new IllegalArgumentException("Cannot find database " + databaseName);
    }
    JdbcDatasourceSettings settings = new JdbcDatasourceSettings();
    SqlDatabase.JdbcDatasourceSettings dbSettings = database.getJdbcDatasourceSettings();
    if(dbSettings != null) {
      BeanUtils.copyProperties(dbSettings, settings, new String[] { "tableSettings" });
      if(dbSettings.getTableSettings() != null) {
        for(SqlDatabase.JdbcDatasourceSettings.JdbcValueTableSettings dbTableSettings : dbSettings.getTableSettings()) {
          JdbcValueTableSettings tableSettings = new JdbcValueTableSettings();
          BeanUtils.copyProperties(dbTableSettings, tableSettings);
          settings.addTableSettings(tableSettings);
        }
      }
    }
    return settings;
  }

  @Override
  public void dispose() {
    databaseRegistry.unregister(databaseName, getName());
  }

}
