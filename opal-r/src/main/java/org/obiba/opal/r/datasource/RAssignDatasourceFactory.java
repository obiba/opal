/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.datasource;

import com.google.common.base.Strings;
import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.opal.spi.r.RServerConnection;

import javax.validation.constraints.NotNull;

public class RAssignDatasourceFactory extends AbstractDatasourceFactory {

  private final String symbol;

  private final RServerConnection rConnection;

  private String idColumnName;

  private boolean withMissings = true;

  public RAssignDatasourceFactory(String name, String symbol, RServerConnection rConnection) {
    setName(name);
    this.symbol = symbol;
    this.rConnection = rConnection;
  }

  public void setIdColumnName(String idColumnName) {
    this.idColumnName = idColumnName;
  }

  public void setWithMissings(boolean withMissings) {
    this.withMissings = withMissings;
  }

  @NotNull
  @Override
  protected Datasource internalCreate() {
    RAssignDatasource ds = new RAssignDatasource(getName(), symbol, rConnection);
    ds.setMultilines(true);
    ds.setEntityIdName(Strings.isNullOrEmpty(idColumnName) ? "id" : idColumnName);
    ds.setWithMissings(withMissings);
    return ds;
  }

}
