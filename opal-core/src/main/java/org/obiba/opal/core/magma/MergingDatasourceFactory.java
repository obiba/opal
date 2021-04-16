/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.magma;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.DatasourceTransformer;

/**
 * Wrap a datasource to merge variable properties and attributes.
 */
public class MergingDatasourceFactory implements DatasourceFactory {

  private final DatasourceFactory wrappedFactory;

  private final Datasource destinationDatasource;

  public MergingDatasourceFactory(DatasourceFactory wrappedFactory, Datasource destinationDatasource) {
    this.wrappedFactory = wrappedFactory;
    this.destinationDatasource = destinationDatasource;
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
  public Datasource create() {
    return new MergingDatasource(wrappedFactory.create(), destinationDatasource);
  }

  @Override
  public void setDatasourceTransformer(DatasourceTransformer transformer) {

  }

  @Override
  public DatasourceTransformer getDatasourceTransformer() {
    return null;
  }
}
