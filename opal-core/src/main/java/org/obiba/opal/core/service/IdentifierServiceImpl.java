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

import org.obiba.magma.AttributeAware;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;
import org.obiba.magma.views.SelectClause;
import org.obiba.magma.views.View;
import org.obiba.opal.core.identifiers.IdentifierGenerator;
import org.obiba.opal.core.identifiers.IdentifiersMapping;
import org.obiba.opal.core.magma.IdentifiersMappingView;
import org.obiba.opal.core.magma.PrivateVariableEntityMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

/**
 *
 */
@Service
public class IdentifierServiceImpl implements IdentifierService {

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private IdentifierGenerator participantIdentifier;

  @Override
  public Variable createIdentifierVariable(@Nullable ValueTable privateView, @NotNull IdentifiersMapping idsMapping) {
    Variable idVariable = Variable.Builder.newVariable(idsMapping.getName(), TextType.get(), idsMapping.getEntityType())
        .build();

    try(ValueTableWriter identifiersTableWriter = identifiersTableService
        .createIdentifiersTableWriter(idsMapping.getEntityType());
        ValueTableWriter.VariableWriter variableWriter = identifiersTableWriter.writeVariables()) {
      // Create private variables
      variableWriter.writeVariable(idVariable);
      if(privateView != null) {
        DatasourceCopier.Builder.newCopier().dontCopyValues().build().copyMetadata(privateView, variableWriter);
      }
    }
    return idVariable;
  }

  @Override
  public View createPrivateView(String viewName, ValueTable dataTable, @Nullable String select) {
    if(select != null) {
      View privateView = View.Builder.newView(viewName, dataTable).select(new JavascriptClause(select)).build();
      privateView.initialise();
      return privateView;
    }
    return View.Builder.newView(viewName, dataTable).select(new SelectClause() {
      @Override
      public boolean select(Variable variable) {
        return isIdentifierVariable(variable);
      }
    }).build();
  }

  @Override
  public IdentifiersMappingView createPublicView(IdentifiersMappingView dataTable, boolean allowIdentifierGeneration,
      boolean ignoreUnknownIdentifier) {

    String idMapping = dataTable.getIdentifiersMapping();
    ValueTable table = dataTable.getWrappedValueTable();

    IdentifiersMappingView publicTable = new IdentifiersMappingView(idMapping, IdentifiersMappingView.Policy.UNIT_IDENTIFIERS_ARE_PRIVATE,
        table, identifiersTableService,
        allowIdentifierGeneration ? participantIdentifier : null, ignoreUnknownIdentifier);
    final String select = identifiersTableService.getSelectScript(table.getEntityType(), idMapping);
    if(!Strings.isNullOrEmpty(select)) {
      publicTable.setSelectClause(new JavascriptClause(select) {

        @Override
        public boolean select(Variable variable) {
          return !isIdentifierVariable(variable) && !isIdentifierVariableForUnit(variable);
        }

        private boolean isIdentifierVariableForUnit(Variable variable) {
          return select != null && super.select(variable);
        }

      });
    }
    publicTable.initialise();
    return publicTable;
  }

  private boolean isIdentifierVariable(@NotNull AttributeAware variable) {
    if(!variable.hasAttribute("identifier")) return false;
    Value value = variable.getAttribute("identifier").getValue();
    return value.equals(BooleanType.get().trueValue()) || "true".equals(value.toString().toLowerCase());
  }

  @Override
  public void copyParticipantIdentifiers(VariableEntity publicEntity, ValueTable privateView,
      PrivateVariableEntityMap entityMap, ValueTableWriter keysTableWriter) {
    VariableEntity privateEntity = entityMap.privateEntity(publicEntity);

    // Copy all other private variable values
    try(ValueTableWriter.ValueSetWriter valueSetWriter = keysTableWriter.writeValueSet(publicEntity)) {
      DatasourceCopier datasourceCopier = DatasourceCopier.Builder.newCopier().dontCopyMetadata().build();
      datasourceCopier.copyValues(privateView, privateView.getValueSet(privateEntity),
          identifiersTableService.getIdentifiersTable(privateView.getEntityType()).getName(), valueSetWriter);
    }
  }

}
