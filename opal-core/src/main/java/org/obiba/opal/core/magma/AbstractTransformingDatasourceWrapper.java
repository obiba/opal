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

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.obiba.magma.Attribute;
import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 *
 */
public abstract class AbstractTransformingDatasourceWrapper implements Datasource {

  protected abstract Datasource getWrappedDatasource();

  protected abstract ValueTable transformValueTable(ValueTable wrappedTable);

  protected abstract ValueTableWriter transformValueTableWritter(ValueTableWriter wrappedTableWritter, String entityType);

  @Override
  public ValueTableWriter createWriter(String tableName, String entityType) {
    return transformValueTableWritter(getWrappedDatasource().createWriter(tableName, entityType), entityType);
  }

  @Override
  public String getName() {
    return getWrappedDatasource().getName();
  }

  @Override
  public String getType() {
    return getWrappedDatasource().getType();
  }

  @Override
  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    return transformValueTable(getWrappedDatasource().getValueTable(name));
  }

  @Override
  public Set<ValueTable> getValueTables() {
    return ImmutableSet.copyOf(Iterables.transform(getWrappedDatasource().getValueTables(), new Function<ValueTable, ValueTable>() {

      @Override
      public ValueTable apply(ValueTable from) {
        return transformValueTable(from);
      }
    }));
  }

  @Override
  public boolean hasValueTable(String name) {
    return getWrappedDatasource().hasValueTable(name);
  }

  @Override
  public void setAttributeValue(String name, Value value) {
    getWrappedDatasource().setAttributeValue(name, value);
  }

  @Override
  public void initialise() {
    getWrappedDatasource().initialise();
  }

  @Override
  public void dispose() {
    getWrappedDatasource().dispose();
  }

  @Override
  public Attribute getAttribute(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttribute(name);
  }

  @Override
  public Attribute getAttribute(String name, Locale locale) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttribute(name, locale);
  }

  @Override
  public String getAttributeStringValue(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributeStringValue(name);
  }

  @Override
  public Value getAttributeValue(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributeValue(name);
  }

  @Override
  public List<Attribute> getAttributes(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributes(name);
  }

  @Override
  public List<Attribute> getAttributes() {
    return getWrappedDatasource().getAttributes();
  }

  @Override
  public boolean hasAttribute(String name) {
    return getWrappedDatasource().hasAttribute(name);
  }

  @Override
  public boolean hasAttribute(String name, Locale locale) {
    return getWrappedDatasource().hasAttribute(name, locale);
  }

  @Override
  public boolean hasAttributes() {
    return getWrappedDatasource().hasAttributes();
  }

  @Override
  public boolean canDropTable(String name) {
    return getWrappedDatasource().canDropTable(name);
  }

  @Override
  public void dropTable(String name) {
    getWrappedDatasource().dropTable(name);
  };
}
