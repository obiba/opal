/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.rest.client.magma;

import javax.annotation.Nonnull;

import org.apache.http.params.CoreConnectionPNames;
import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaRuntimeException;

/**
 * Factory of a datasource that connects to Opal using its RESTful services.
 */
public class RestDatasourceFactory extends AbstractDatasourceFactory {

  private final String url;

  private final String username;

  private final String password;

  private final String remoteDatasource;

  private Integer soTimeout;

  public RestDatasourceFactory(String name, String url, String username, String password, String remoteDatasource) {
    setName(name);
    this.url = url;
    this.username = username;
    this.password = password;
    this.remoteDatasource = remoteDatasource;
  }

  /**
   * @see CoreConnectionPNames#SO_TIMEOUT
   */
  @SuppressWarnings("UnusedDeclaration")
  public void setSoTimeout(int soTimeout) {
    this.soTimeout = soTimeout;
  }

  private OpalJavaClient createOpalJavaClient() throws Exception {
    if(url == null || url.isEmpty()) throw new IllegalStateException("Opal url cannot be empty.");

    String opalUrl = url;
    if(!url.endsWith("/ws") || !url.endsWith("/ws/")) {
      opalUrl = url + "/ws";
    }
    OpalJavaClient client = new OpalJavaClient(opalUrl, username, password);
    if(soTimeout != null) {
      client.setSoTimeout(soTimeout);
    }
    return client;
  }

  @Nonnull
  @Override
  protected Datasource internalCreate() {
    try {
      return new RestDatasource(getName(), createOpalJavaClient(), remoteDatasource);
    } catch(Exception e) {
      throw new MagmaRuntimeException(e);
    }
  }

}
