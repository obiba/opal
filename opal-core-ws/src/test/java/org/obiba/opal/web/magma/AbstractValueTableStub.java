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

import java.util.Collections;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;

abstract class AbstractValueTableStub implements ValueTable {

  public Datasource getDatasource() {
    return new AbstractDatasourceStub() {
    };
  }

  public String getEntityType() {
    return "Participant";
  }

  public String getName() {
    return "tableStub";
  }

  public Timestamps getTimestamps(ValueSet valueSet) {
    return null;
  }

  public Value getValue(Variable variable, ValueSet valueSet) {
    return null;
  }

  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return null;
  }

  public Iterable<ValueSet> getValueSets() {
    return null;
  }

  public Variable getVariable(String name) throws NoSuchVariableException {
    return null;
  }

  public Set<VariableEntity> getVariableEntities() {
    return Collections.emptySet();
  }

  public VariableValueSource getVariableValueSource(String name) throws NoSuchVariableException {
    return null;
  }

  public Iterable<Variable> getVariables() {
    return Collections.emptyList();
  }

  public boolean hasValueSet(VariableEntity entity) {
    return false;
  }

  public boolean hasVariable(String name) {
    return false;
  }

  public boolean isForEntityType(String entityType) {
    return getEntityType().equals(entityType);
  }
}