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

import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.views.SelectClause;
import org.obiba.magma.views.View;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 *
 */
public class PrivateVariableEntityValueTable extends View {
  //
  // Instance Variables
  //

  private PrivateVariableEntityMap privateMap;

  private SelectClause privateSelectClause;

  //
  // Constructors
  //

  /**
   * Constructor.
   * 
   * @param name name of this table
   * @param privateTable wrapped value table
   * @param privateMap bidirectional entity map (public <--> private)
   * @param privateSelectClause selects variables that should part of the private entry in the bidirectional map
   */
  public PrivateVariableEntityValueTable(String name, ValueTable privateTable, PrivateVariableEntityMap privateMap, SelectClause privateSelectClause) {
    super(name, privateTable);

    this.privateMap = privateMap;
    this.privateSelectClause = privateSelectClause;
  }

  //
  // View Methods
  //

  /**
   * Override <code>hasValueSet</code> to map the specified "public" entity to its "private" entity.
   */
  @Override
  public boolean hasValueSet(VariableEntity publicEntity) {
    return super.hasValueSet(privateMap.privateEntity(publicEntity));
  }

  /**
   * Override <code>getValueSet</code> to return the {@link ValueSet} with a reference to its "public" entity.
   */
  @Override
  public ValueSet getValueSet(VariableEntity publicEntity) throws NoSuchValueSetException {
    return super.getValueSet(privateMap.privateEntity(publicEntity));
  }

  /**
   * Override <code>getValueSetTransformer</code> so that the {@link ValueSet} returned references its "public" entity.
   */
  @Override
  protected Function<ValueSet, ValueSet> getValueSetTransformer() {
    return new Function<ValueSet, ValueSet>() {
      public ValueSet apply(ValueSet from) {
        VariableEntity publicEntity = privateMap.publicEntity(from.getVariableEntity());
        if(publicEntity == null) {
          publicEntity = privateMap.createPublicEntity(from, getPrivateVariableValueSources());
        }
        return new ValueSetBean(PrivateVariableEntityValueTable.this, publicEntity);
      }
    };
  }

  //
  // Methods
  //

  private Iterable<Variable> getPrivateVariables() {
    Iterable<Variable> variables = getWrappedValueTable().getVariables();
    if(privateSelectClause != null) {
      Iterable<Variable> filteredVariables = Iterables.filter(variables, new Predicate<Variable>() {
        public boolean apply(Variable input) {
          return privateSelectClause.select(input);
        }
      });
      return filteredVariables;
    }

    return variables;
  }

  private Iterable<VariableValueSource> getPrivateVariableValueSources() {
    return Iterables.transform(getPrivateVariables(), new Function<Variable, VariableValueSource>() {
      public VariableValueSource apply(Variable from) {
        return getVariableValueSource(from.getName());
      }
    });
  }
}
