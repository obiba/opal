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

import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.transform.BijectiveFunction;
import org.obiba.magma.views.View;

/**
 * A view that uses a {@code PrivateVariableEntityMap} instance to map private entity identifiers to public ones and
 * only expose public identifiers.
 */
public class PrivateVariableEntityValueTable extends View {
  //
  // Instance Variables
  //

  private PrivateVariableEntityMap privateMap;

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
  public PrivateVariableEntityValueTable(String name, ValueTable privateTable, PrivateVariableEntityMap privateMap) {
    super(name, privateTable);

    this.privateMap = privateMap;
  }

  /**
   * Override <code>getVariableEntityMappingFunction</code> to transform "private" entities into "public" entities.
   */
  public BijectiveFunction<VariableEntity, VariableEntity> getVariableEntityMappingFunction() {
    return new BijectiveFunction<VariableEntity, VariableEntity>() {
      public VariableEntity apply(VariableEntity privateEntity) {
        VariableEntity publicEntity = privateMap.publicEntity(privateEntity);
        if(publicEntity == null) {
          publicEntity = privateMap.createPublicEntity(privateEntity);
        }
        return publicEntity;
      }

      @Override
      public VariableEntity unapply(VariableEntity publicEntity) {
        return privateMap.privateEntity(publicEntity);
      }
    };
  }
}
