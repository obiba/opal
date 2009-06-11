/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.sesame.repository.rdbms;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Iterator;

import javax.imageio.spi.ServiceRegistry;
import javax.sql.DataSource;

import org.openrdf.sail.SailException;
import org.openrdf.sail.rdbms.RdbmsConnectionFactory;
import org.openrdf.sail.rdbms.RdbmsProvider;
import org.openrdf.sail.rdbms.RdbmsStore;

/**
 * A {@code RdbmsStore} implementation on top of {@code DataSource} that uses database metadata to determine the
 * appropriate {@code RdbmsConnectionFactory} to use. This implementation is useful when the the {@code DataSource}
 * implementation is not {@code BasicDataSource} from commons-dbcp.
 */
public class DatasourceStore extends RdbmsStore {

  protected DataSource datasource;

  public void setDataSource(DataSource datasource) {
    this.datasource = datasource;
  }

  @Override
  public void initialize() throws SailException {
    try {
      RdbmsConnectionFactory factory = initializeFactory();
      super.setConnectionFactory(factory);
    } catch(SQLException e) {
      throw new SailException(e);
    }
    super.initialize();
  }

  /**
   * Create and initialize the {@code RdbmsConnectionFactory}.
   * 
   * @return
   * @throws SQLException
   */
  protected RdbmsConnectionFactory initializeFactory() throws SQLException {
    Connection con = datasource.getConnection();
    try {
      DatabaseMetaData metaData = con.getMetaData();
      RdbmsConnectionFactory factory = newFactory(metaData);
      factory.setSail(this);
      factory.setDataSource(datasource);
      return factory;
    } finally {
      con.close();
    }
  }

  /**
   * Builds a new {@code RdbmsConnectionFactory} from the provided {@code DatabaseMetaData}.
   * 
   * @see {@link RdbmsProvider}
   * @see {@link RdbmsStore#newFactory}
   * @param metaData
   * @return
   * @throws SQLException
   */
  protected RdbmsConnectionFactory newFactory(DatabaseMetaData metaData) throws SQLException {
    String dbn = metaData.getDatabaseProductName();
    String dbv = metaData.getDatabaseProductVersion();
    RdbmsConnectionFactory factory;
    Iterator<RdbmsProvider> providers;
    providers = ServiceRegistry.lookupProviders(RdbmsProvider.class);
    while(providers.hasNext()) {
      RdbmsProvider provider = providers.next();
      factory = provider.createRdbmsConnectionFactory(dbn, dbv);
      if(factory != null) {
        return factory;
      }
    }
    return new RdbmsConnectionFactory();
  }
}
