/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service.impl;

import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.opal.core.magma.PrivateVariableEntityMap;
import org.obiba.opal.core.service.IOpalKeyRegistry;

/**
 * An Opal implementation of {@link PrivateVariableEntityMap}, on top of {@link IOpalKeyRegistry}.
 */
public class OpalPrivateVariableEntityMap implements PrivateVariableEntityMap {
  //
  // Instance Variables
  //

  private String owner;

  private IOpalKeyRegistry opalKeyRegistry;

  //
  // PrivateVariableEntityMap Methods
  //

  public VariableEntity publicEntity(VariableEntity privateEntity) {
    String publicIdentifier = opalKeyRegistry.findOpalKey(owner, privateEntity.getIdentifier());
    if(publicIdentifier != null) {
      return new VariableEntityBean(privateEntity.getType(), publicIdentifier);
    }
    return null;
  }

  public VariableEntity privateEntity(VariableEntity publicEntity) {
    String privateIdentifier = opalKeyRegistry.findOwnerKey(owner, publicEntity.getIdentifier());
    if(privateIdentifier != null) {
      return new VariableEntityBean(publicEntity.getType(), privateIdentifier);
    }
    return null;
  }

  public boolean hasPrivateEntity(VariableEntity privateEntity) {
    return opalKeyRegistry.hasOpalKey(owner, privateEntity.getIdentifier());
  }

  public VariableEntity createPublicEntity(ValueSet privateValueSet, Iterable<VariableValueSource> privateVariables) {
    // Create the "public" entity.
    String publicIdentifier = opalKeyRegistry.registerNewOpalKey(owner, privateValueSet.getVariableEntity().getIdentifier());

    // Persist private variables as keys in the opal key database.
    for(VariableValueSource variableValueSource : privateVariables) {
      if(!variableValueSource.getValue(privateValueSet).isNull()) {
        opalKeyRegistry.registerKey(publicIdentifier, variableValueSource.getVariable().getName(), variableValueSource.getValue(privateValueSet).toString());
      }
    }

    return new VariableEntityBean(privateValueSet.getVariableEntity().getType(), publicIdentifier);
  }

  //
  // Methods
  //

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public void setOpalKeyRegistry(IOpalKeyRegistry opalKeyRegistry) {
    this.opalKeyRegistry = opalKeyRegistry;
  }
}
