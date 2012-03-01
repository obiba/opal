/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.support;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.datasource.fs.support.FsDatasourceFactory;

/**
 *
 */
public class OnyxDatasourceFactory extends AbstractDatasourceFactory {

  private final FsDatasourceFactory wrappedFactory;

  public OnyxDatasourceFactory(FsDatasourceFactory wrappedFactory) {
    super();
    this.wrappedFactory = wrappedFactory;
  }

  @Override
  public void setName(String name) {
    wrappedFactory.setName(name);
  }

  @Override
  public String getName() {
    return wrappedFactory.getName();
  }

  @Override
  protected Datasource internalCreate() {
    return new OnyxDatasource((FsDatasource) wrappedFactory.create());
  }

}
