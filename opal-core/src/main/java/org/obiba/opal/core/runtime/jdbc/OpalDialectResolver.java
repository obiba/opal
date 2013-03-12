/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.dialect.resolver.AbstractDialectResolver;

/**
 * Ensures usage of InnoDB for MySQL databases and uses custom dialect for HSQLDB, otherwise, fallback to default
 * behavior.
 * <p/>
 * This class is instantiated by Hibernate itself through the {@code hibernate.properties} file.
 */
public class OpalDialectResolver extends AbstractDialectResolver {

  @Override
  protected Dialect resolveDialectInternal(DatabaseMetaData metaData) throws SQLException {
    String databaseName = metaData.getDatabaseProductName();
    int databaseMajorVersion = metaData.getDatabaseMajorVersion();

    if("HSQL Database Engine".equals(databaseName)) {
      return new MagmaHSQLDialect();
    }

    if("MySQL".equals(databaseName) && databaseMajorVersion > 4) {
      return new MySQL5InnoDBDialect();
    }

    return null;
  }

}
