/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.AttributeAware;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.lang.Closeables;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;
import org.obiba.magma.views.SelectClause;
import org.obiba.magma.views.View;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.magma.FunctionalUnitView;
import org.obiba.opal.core.magma.PrivateVariableEntityMap;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class IdentifierServiceImpl implements IdentifierService {

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private IParticipantIdentifier participantIdentifier;

  /**
   * Creates a {@link org.obiba.magma.views.View} of the participant table's "private" variables (i.e., identifiers).
   *
   * @param viewName
   * @param participantTable
   * @return
   */
  @Override
  public View createPrivateView(String viewName, ValueTable participantTable, FunctionalUnit unit,
      @Nullable String select) {
    if(select != null) {
      View privateView = View.Builder.newView(viewName, participantTable).select(new JavascriptClause(select)).build();
      privateView.initialise();
      return privateView;
    }
    if(unit.getSelect() != null) {
      View privateView = View.Builder.newView(viewName, participantTable).select(unit.getSelect()).build();
      privateView.initialise();
      return privateView;
    }
    return View.Builder.newView(viewName, participantTable).select(new SelectClause() {
      @Override
      public boolean select(Variable variable) {
        return isIdentifierVariable(variable);
      }
    }).build();
  }

  /**
   * Wraps the participant table in a {@link org.obiba.magma.views.View} that exposes public entities and non-identifier variables.
   *
   * @param participantTable
   * @param unit
   * @param allowIdentifierGeneration
   * @return
   */
  @Override
  public FunctionalUnitView createPublicView(FunctionalUnitView participantTable, boolean allowIdentifierGeneration,
      boolean ignoreUnknownIdentifier) {

    final FunctionalUnit unit = participantTable.getUnit();

    FunctionalUnitView publicTable = new FunctionalUnitView(unit,
        FunctionalUnitView.Policy.UNIT_IDENTIFIERS_ARE_PRIVATE, participantTable,
        identifiersTableService.getValueTable(), allowIdentifierGeneration ? participantIdentifier : null,
        ignoreUnknownIdentifier);
    publicTable.setSelectClause(new SelectClause() {

      @Override
      public boolean select(Variable variable) {
        return !isIdentifierVariable(variable) && !isIdentifierVariableForUnit(variable);
      }

      private boolean isIdentifierVariableForUnit(Variable variable) {
        return unit.getSelect() != null && unit.getSelect().select(variable);
      }

    });
    publicTable.initialise();
    return publicTable;
  }

  /**
   * Write the key variable.
   *
   * @param privateView
   * @param keyVariableName
   * @return
   * @throws java.io.IOException
   */
  @Override
  public Variable createKeyVariable(@Nullable ValueTable privateView, @Nonnull String keyVariableName) {

    Variable keyVariable = Variable.Builder
        .newVariable(keyVariableName, TextType.get(), identifiersTableService.getEntityType()).build();

    ValueTableWriter identifiersTableWriter = identifiersTableService.createValueTableWriter();
    try {
      ValueTableWriter.VariableWriter variableWriter = identifiersTableWriter.writeVariables();
      try {
        // Create private variables
        variableWriter.writeVariable(keyVariable);
        if(privateView != null) {
          DatasourceCopier.Builder.newCopier().dontCopyValues().build().copyMetadata(privateView, variableWriter);
        }
      } finally {
        Closeables.closeQuietly(variableWriter);
      }
    } finally {
      Closeables.closeQuietly(identifiersTableWriter);
    }
    return keyVariable;
  }

  private boolean isIdentifierVariable(@Nonnull AttributeAware variable) {
    if(!variable.hasAttribute("identifier")) return false;
    Value value = variable.getAttribute("identifier").getValue();
    return value.equals(BooleanType.get().trueValue()) || "true".equals(value.toString().toLowerCase());
  }

  @Override
  public void copyParticipantIdentifiers(VariableEntity publicEntity, ValueTable privateView,
      PrivateVariableEntityMap entityMap, ValueTableWriter keysTableWriter) {
    VariableEntity privateEntity = entityMap.privateEntity(publicEntity);

    ValueTableWriter.ValueSetWriter valueSetWriter = keysTableWriter.writeValueSet(publicEntity);
    try {
      // Copy all other private variable values
      DatasourceCopier datasourceCopier = DatasourceCopier.Builder.newCopier().dontCopyMetadata().build();
      datasourceCopier.copyValues(privateView, privateView.getValueSet(privateEntity),
          identifiersTableService.getValueTable().getName(), valueSetWriter);
    } finally {
      Closeables.closeQuietly(valueSetWriter);
    }
  }

}
