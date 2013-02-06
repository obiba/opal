/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.magma;

import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.AbstractDatasourceWrapper;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 *
 */
public abstract class AbstractTransformingDatasourceWrapper extends AbstractDatasourceWrapper {

  protected abstract ValueTable transformValueTable(ValueTable wrappedTable);

  protected abstract ValueTableWriter transformValueTableWriter(ValueTableWriter wrappedTableWriter, String entityType);

  protected AbstractTransformingDatasourceWrapper(@Nonnull Datasource wrapped) {
    super(wrapped);
  }

  @Override
  public ValueTableWriter createWriter(String tableName, String entityType) {
    return transformValueTableWriter(getWrappedDatasource().createWriter(tableName, entityType), entityType);
  }

  @Override
  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    return transformValueTable(getWrappedDatasource().getValueTable(name));
  }

  @Override
  public Set<ValueTable> getValueTables() {
    return ImmutableSet
        .copyOf(Iterables.transform(getWrappedDatasource().getValueTables(), new Function<ValueTable, ValueTable>() {

          @Override
          public ValueTable apply(ValueTable from) {
            return transformValueTable(from);
          }
        }));
  }

}
