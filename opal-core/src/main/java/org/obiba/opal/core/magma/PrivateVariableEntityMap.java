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

import javax.annotation.Nonnull;

import org.obiba.magma.VariableEntity;

/**
 * Interface that defines the contract of mapping private VariableEntity to public instances and for creating public
 * identifiers if possible.
 */
public interface PrivateVariableEntityMap {

  VariableEntity publicEntity(@Nonnull VariableEntity privateEntity);

  boolean hasPublicEntity(@Nonnull VariableEntity publicEntity);

  VariableEntity privateEntity(@Nonnull VariableEntity publicEntity);

  boolean hasPrivateEntity(@Nonnull VariableEntity privateEntity);

  VariableEntity createPublicEntity(@Nonnull VariableEntity privateEntity);

  VariableEntity createPrivateEntity(@Nonnull VariableEntity publicEntity);
}
