/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service;

import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.magma.VariableEntity;
import org.springframework.util.Assert;

/**
 * Thrown when VariableEntities are expected to exist in Opal, but can not be found. This occurs when importing data
 * "as-is". In this case all the imported identifiers are expected to already exist within Opal.
 */
public class NonExistentVariableEntitiesException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  @Nonnull
  private final Set<VariableEntity> nonExistentVariableEntities;

  public NonExistentVariableEntitiesException(@Nonnull Set<VariableEntity> nonExistentVariableEntities) {
    Assert.notNull(nonExistentVariableEntities, "nonExistentVariableEntities must not be null");
    this.nonExistentVariableEntities = nonExistentVariableEntities;
  }

  @Override
  public String getMessage() {
    StringBuilder sb = new StringBuilder();
    sb.append("Could not find the following public identifiers in the keys database: ");
    sb.append(getNonExistentIdentifiers());
    return sb.toString();
  }

  public String getNonExistentIdentifiers() {
    StringBuilder sb = new StringBuilder();
    for(VariableEntity variableEntity : nonExistentVariableEntities) {
      sb.append(variableEntity.getIdentifier()).append(",");
    }
    sb.setLength(sb.length() - 1); // remove last comma.
    return sb.toString();
  }

}
