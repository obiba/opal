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

import javax.sql.DataSource;

import org.hibernate.SessionFactory;

public interface JdbcDataSourceRegistry {

  Iterable<JdbcDataSource> listDataSources();

  JdbcDataSource getJdbcDataSource(String name);

  DataSource getDataSource(String name, String usedBy);

  SessionFactory getSessionFactory(String name, String usedBy);

  void update(JdbcDataSource jdbcDataSource);

  void remove(JdbcDataSource jdbcDataSource);

  void registerDataSource(JdbcDataSource jdbcDatasource);

  void unregister(String databaseName, String usedBy);

}
