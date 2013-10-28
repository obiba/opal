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
import javax.annotation.Nullable;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.Disposable;
import org.obiba.magma.datasource.limesurvey.LimesurveyDatasource;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

public class DatabaseLimesurveyDatasourceFactory extends AbstractDatasourceFactory implements Disposable {

  private String databaseName;

  // transient because of XML serialization
  @SuppressWarnings("TransientFieldInNonSerializableClass")
  @Autowired
  private transient DatabaseRegistry databaseRegistry;

  // empty public constructor because of XML serialization
  @SuppressWarnings("UnusedDeclaration")
  public DatabaseLimesurveyDatasourceFactory() {
  }

  /**
   * @param name
   * @param databaseName
   * @param tablePrefix
   * @param dataSourceRegistry
   */
  public DatabaseLimesurveyDatasourceFactory(@Nonnull String name, @Nonnull String databaseName,
      DatabaseRegistry databaseRegistry) {
    Assert.notNull(name);
    Assert.notNull(databaseName);
    Assert.notNull(databaseRegistry);
    setName(name);
    this.databaseName = databaseName;
    this.databaseRegistry = databaseRegistry;
  }

  @Nonnull
  @Override
  protected Datasource internalCreate() {
    SqlSettings.LimesurveyDatasourceSettings settings = getSettings();
    return new LimesurveyDatasource(getName(), databaseRegistry.getDataSource(databaseName, getName()),
        settings == null ? null : settings.getTablePrefix());
  }

  @Nullable
  private SqlSettings.LimesurveyDatasourceSettings getSettings() {
    SqlSettings sqlSettings = databaseRegistry.getDatabase(databaseName).getSqlSettings();
    if(sqlSettings == null) {
      throw new IllegalArgumentException("Cannot find SqlSettings for database " + databaseName);
    }
    return sqlSettings.getLimesurveyDatasourceSettings();
  }

  @Override
  public void dispose() {
    databaseRegistry.unregister(databaseName, getName());
  }

}
