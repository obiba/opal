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

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.Disposable;
import org.obiba.magma.datasource.jdbc.JdbcDatasource;
import org.obiba.magma.datasource.jdbc.JdbcDatasourceSettings;
import org.obiba.opal.core.runtime.jdbc.JdbcDataSourceRegistry;
import org.springframework.beans.factory.annotation.Autowired;

public class DatabaseJdbcDatasourceFactory extends AbstractDatasourceFactory implements Disposable {

  private String databaseName;

  private JdbcDatasourceSettings settings;

  @Autowired
  private transient JdbcDataSourceRegistry dataSourceRegistry;

  public DatabaseJdbcDatasourceFactory() {
  }

  /**
   * @param name
   * @param jdbcDataSourceRegistry
   * @param parseSettings
   */
  public DatabaseJdbcDatasourceFactory(String name, String databaseName, JdbcDatasourceSettings settings, JdbcDataSourceRegistry dataSourceRegistry) {
    super.setName(name);
    this.databaseName = databaseName;
    this.settings = settings;
    this.dataSourceRegistry = dataSourceRegistry;
  }

  @Override
  protected Datasource internalCreate() {
    return new JdbcDatasource(getName(), dataSourceRegistry.getDataSource(databaseName, getName()), settings);
  }

  @Override
  public void dispose() {
    dataSourceRegistry.unregister(databaseName, getName());
  }

}
