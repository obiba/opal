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
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.MultiplexingDatasource;
import org.obiba.magma.support.MultiplexingDatasource.VariableAttributeMultiplexer;
import org.obiba.magma.support.MultiplexingDatasource.VariableNameTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fs Datasource wrapper that tries to detect old Onyx data dictionary, and in that case applies multiplexes the
 * Participants table and rename some variables.
 */
public class OnyxDatasource implements Datasource {

  private static final Logger log = LoggerFactory.getLogger(OnyxDatasource.class);

  private final FsDatasource fsDatasource;

  private Datasource wrapped;

  public OnyxDatasource(FsDatasource fsDatasource) {
    super();
    this.fsDatasource = fsDatasource;
  }

  @Override
  public void initialise() {
    MultiplexingDatasource mxDatasource = new MultiplexingDatasource(fsDatasource, new VariableAttributeMultiplexer("stage"), new VariableNameTransformer() {

      @Override
      protected String transformName(Variable variable) {
        if(variable.hasAttribute("stage")) {
          return variable.getName().replaceFirst("^.*\\.?" + variable.getAttributeStringValue("stage") + "\\.", "");
        }
        return variable.getName();
      }

    });
    try {
      // this will initialise the fs datasource
      Initialisables.initialise(mxDatasource);
      boolean isOnyx = false;
      int participantTablesCount = 0;
      for(ValueTable table : fsDatasource.getValueTables()) {
        if(table.getEntityType().equals("Participant")) {
          participantTablesCount++;
          if(table.getName().equals("Participants")) {
            if(table.hasVariable("Admin.onyxVersion")) {
              isOnyx = true;
            }
          }
        }
      }
      if(isOnyx && participantTablesCount == 1) {
        wrapped = mxDatasource;
      } else {
        wrapped = fsDatasource;
      }
    } catch(UnsupportedOperationException ex) {
      wrapped = fsDatasource;
    }
  }

  @Override
  public void dispose() {
    Disposables.dispose(wrapped);
  }

  //
  // Attributes methods
  //

  @Override
  public boolean hasAttributes() {
    return wrapped.hasAttributes();
  }

  @Override
  public boolean hasAttribute(String name) {
    return wrapped.hasAttribute(name);
  }

  @Override
  public Attribute getAttribute(String name) throws NoSuchAttributeException {
    return wrapped.getAttribute(name);
  }

  @Override
  public boolean hasAttribute(String name, Locale locale) {
    return wrapped.hasAttribute(name, locale);
  }

  @Override
  public Attribute getAttribute(String name, Locale locale) throws NoSuchAttributeException {
    return wrapped.getAttribute(name, locale);
  }

  @Override
  public Value getAttributeValue(String name) throws NoSuchAttributeException {
    return wrapped.getAttributeValue(name);
  }

  @Override
  public String getAttributeStringValue(String name) throws NoSuchAttributeException {
    return wrapped.getAttributeStringValue(name);
  }

  @Override
  public List<Attribute> getAttributes(String name) throws NoSuchAttributeException {
    return wrapped.getAttributes(name);
  }

  @Override
  public List<Attribute> getAttributes() {
    return wrapped.getAttributes();
  }

  @Override
  public void setAttributeValue(String name, Value value) {
    wrapped.setAttributeValue(name, value);
  }

  @Override
  public String getName() {
    return wrapped.getName();
  }

  @Override
  public String getType() {
    return wrapped.getType();
  }

  @Override
  public boolean hasValueTable(String name) {
    return wrapped.hasValueTable(name);
  }

  @Override
  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    return wrapped.getValueTable(name);
  }

  @Override
  public Set<ValueTable> getValueTables() {
    return wrapped.getValueTables();
  }

  @Override
  public boolean canDropTable(String name) {
    return wrapped.canDropTable(name);
  }

  @Override
  public void dropTable(String name) {
    wrapped.dropTable(name);
  }

  @Override
  public ValueTableWriter createWriter(String tableName, String entityType) {
    return wrapped.createWriter(tableName, entityType);
  }

}
