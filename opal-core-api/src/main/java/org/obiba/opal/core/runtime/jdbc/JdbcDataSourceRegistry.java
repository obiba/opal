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

  public Iterable<JdbcDataSource> listDataSources();

  public JdbcDataSource getJdbcDataSource(String name);

  public DataSource getDataSource(String name, String usedBy);

  public SessionFactory getSessionFactory(String name, String usedBy);

  public void update(JdbcDataSource jdbcDataSource);

  public void remove(JdbcDataSource jdbcDataSource);

  public void registerDataSource(JdbcDataSource jdbcDatasource);

  public void unregister(String databaseName, String usedBy);

}
