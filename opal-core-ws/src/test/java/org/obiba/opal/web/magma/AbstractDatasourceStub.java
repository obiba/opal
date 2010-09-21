/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

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

public abstract class AbstractDatasourceStub implements Datasource {

  public ValueTableWriter createWriter(String tableName, String entityType) {
    return null;
  }

  public String getName() {
    return "datasourceStub";
  }

  public String getType() {
    return null;
  }

  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    return null;
  }

  public Set<ValueTable> getValueTables() {
    return null;
  }

  public boolean hasValueTable(String name) {
    return false;
  }

  public void setAttributeValue(String name, Value value) {
  }

  public void initialise() {
  }

  public void dispose() {
  }

  public Attribute getAttribute(String name) throws NoSuchAttributeException {
    return null;
  }

  public Attribute getAttribute(String name, Locale locale) throws NoSuchAttributeException {
    return null;
  }

  public String getAttributeStringValue(String name) throws NoSuchAttributeException {
    return null;
  }

  public Value getAttributeValue(String name) throws NoSuchAttributeException {
    return null;
  }

  public List<Attribute> getAttributes(String name) throws NoSuchAttributeException {
    return null;
  }

  public List<Attribute> getAttributes() {
    return null;
  }

  public boolean hasAttribute(String name) {
    return false;
  }

  public boolean hasAttribute(String name, Locale locale) {
    return false;
  }

  public boolean hasAttributes() {
    return false;
  }
}
