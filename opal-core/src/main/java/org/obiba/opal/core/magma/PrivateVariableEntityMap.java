/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.magma;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Disposable;
import org.obiba.magma.VariableEntity;

import java.util.List;

/**
 * Interface that defines the contract of mapping private VariableEntity to public instances and for creating public
 * identifiers if possible.
 */
public interface PrivateVariableEntityMap extends Disposable {

  VariableEntity publicEntity(@NotNull VariableEntity privateEntity);

  boolean hasPublicEntity(@NotNull VariableEntity publicEntity);

  VariableEntity privateEntity(@NotNull VariableEntity publicEntity);

  boolean hasPrivateEntity(@NotNull VariableEntity privateEntity);

  VariableEntity createPublicEntity(@NotNull VariableEntity privateEntity);

  VariableEntity createPrivateEntity(@NotNull VariableEntity publicEntity);

  List<VariableEntity> createPrivateEntities(@NotNull List<VariableEntity> publicEntities);
}
