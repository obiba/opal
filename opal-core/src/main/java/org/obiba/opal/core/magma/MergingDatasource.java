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
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasourceWrapper;

import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Datasource which tables are merging their characteristics with the ones of the table with the same name in the
 * destination datasource.
 */
public class MergingDatasource extends AbstractDatasourceWrapper {

  private final Datasource destinationDatasource;

  protected MergingDatasource(@NotNull Datasource wrapped, Datasource destinationDatasource) {
    super(wrapped);
    this.destinationDatasource = destinationDatasource;
  }

  @Override
  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    return destinationDatasource.hasValueTable(name) ?
        new MergingValueTable(super.getValueTable(name), destinationDatasource.getValueTable(name)) :
        super.getValueTable(name);
  }

  @Override
  public Set<ValueTable> getValueTables() {
    return StreamSupport.stream(super.getValueTables().spliterator(), false)
        .map(tbl -> getValueTable(tbl.getName()))
        .collect(Collectors.toSet());
  }
}

