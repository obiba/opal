/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.runtime.jdbc;

import javax.validation.constraints.NotNull;

import org.hibernate.SessionFactory;
import org.obiba.magma.Disposable;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

public class DatabaseSessionFactoryProvider implements SessionFactoryProvider, Disposable {

  private String datasourceName;

  private String databaseName;

  // need to be transient because of XML serialization
  @SuppressWarnings("TransientFieldInNonSerializableClass")
  @Autowired
  private transient DatabaseRegistry databaseRegistry;

  // Public constructor for XStream de-ser.
  @SuppressWarnings("UnusedDeclaration")
  public DatabaseSessionFactoryProvider() {

  }

  @SuppressWarnings("ConstantConditions")
  public DatabaseSessionFactoryProvider(@NotNull String datasourceName, @NotNull DatabaseRegistry databaseRegistry,
      @NotNull String databaseName) {
    Preconditions.checkArgument(datasourceName != null);
    Preconditions.checkArgument(databaseRegistry != null);
    Preconditions.checkArgument(databaseName != null);
    this.datasourceName = datasourceName;
    this.databaseName = databaseName;
    this.databaseRegistry = databaseRegistry;
  }

  @Override
  public SessionFactory getSessionFactory() {
    return databaseRegistry.getSessionFactory(databaseName, datasourceName);
  }

  @Override
  public void dispose() {
    databaseRegistry.unregister(databaseName, datasourceName);
  }
}
