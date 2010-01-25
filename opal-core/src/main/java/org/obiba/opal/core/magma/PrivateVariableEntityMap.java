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

import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;

/**
 * Interface that defines the contract of mapping private VariableEntity to public instances and for creating public
 * identifiers if possible.
 */
public interface PrivateVariableEntityMap {

  public VariableEntity publicEntity(VariableEntity privateEntity);

  public VariableEntity privateEntity(VariableEntity publicEntity);

  public boolean hasPrivateEntity(VariableEntity privateEntity);

  public VariableEntity createPublicEntity(ValueSet privateValueSet, Iterable<VariableValueSource> privateVariables);
}
