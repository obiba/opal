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

import java.util.Collections;

import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
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
   * Override <code>getVariableEntityTransformer</code> to transform "private" entities into "public" entities.
   */
  public Function<VariableEntity, VariableEntity> getVariableEntityTransformer() {
    return new Function<VariableEntity, VariableEntity>() {
      public VariableEntity apply(VariableEntity from) {
        VariableEntity publicEntity = privateMap.publicEntity(from);
        if(publicEntity == null) {
          throw new RuntimeException("Private entity " + from + " not mapped to a public entity");
        }
        return publicEntity;
      }
    };
  }

  /**
   * Override <code>getValueSetTransformer</code> to create the "public" entity if necessary.
   */
  @Override
  public Function<ValueSet, ValueSet> getValueSetTransformer() {
    final Function<ValueSet, ValueSet> baseTransformer = super.getValueSetTransformer();

    return new Function<ValueSet, ValueSet>() {
      public ValueSet apply(ValueSet from) {
        VariableEntity publicEntity = privateMap.publicEntity(from.getVariableEntity());
        if(publicEntity == null) {
          publicEntity = privateMap.createPublicEntity(from, getPrivateVariableValueSources());
        }
        return baseTransformer.apply(from);
      }
    };
  }

  //
  // Methods
  //

  private Iterable<Variable> getPrivateVariables() {
    Iterable<Variable> privateVariables = Collections.emptyList();
    if(privateSelectClause != null) {
      Iterable<Variable> variables = getWrappedValueTable().getVariables();
      privateVariables = Iterables.filter(variables, new Predicate<Variable>() {
        public boolean apply(Variable input) {
          return privateSelectClause.select(input);
        }
      });
    }

    return privateVariables;
  }

  private Iterable<VariableValueSource> getPrivateVariableValueSources() {
    return Iterables.transform(getPrivateVariables(), new Function<Variable, VariableValueSource>() {
      public VariableValueSource apply(Variable from) {
        return getVariableValueSource(from.getName());
      }
    });
  }
}
