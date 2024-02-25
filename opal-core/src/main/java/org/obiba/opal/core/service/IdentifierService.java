/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service;

import jakarta.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.views.View;
import org.obiba.opal.core.identifiers.IdentifiersMapping;
import org.obiba.opal.core.magma.IdentifiersMappingView;
import org.obiba.opal.core.magma.PrivateVariableEntityMap;

/**
 *
 */
public interface IdentifierService {

  Variable createIdentifierVariable(@Nullable ValueTable privateView, @NotNull IdentifiersMapping idsMapping);

  /**
   * Make a view
   * @param viewName
   * @param dataTable
   * @param select
   * @return
   */
  View createPrivateView(String viewName, ValueTable dataTable, @Nullable String select);

  /**
   * Write the key variable and the identifier variables values; update the participant key private/public map.
   */
  void copyParticipantIdentifiers(VariableEntity publicEntity, ValueTable privateView,
      PrivateVariableEntityMap entityMap, ValueTableWriter keysTableWriter);

  /**
   * Wraps the data table in a {@link org.obiba.magma.views.View} that exposes public entities and non-identifier variables.
   *
   * @param dataTable
   * @param unit
   * @param allowIdentifierGeneration
   * @return
   */
  IdentifiersMappingView createPublicView(IdentifiersMappingView dataTable, boolean allowIdentifierGeneration,
      boolean ignoreUnknownIdentifier);
}
