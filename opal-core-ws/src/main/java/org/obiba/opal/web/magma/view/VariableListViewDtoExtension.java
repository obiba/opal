/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.view;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.js.views.VariablesClause;
import org.obiba.magma.support.ValueTableReference;
import org.obiba.magma.support.ValueTableWrapper;
import org.obiba.magma.views.JoinTable;
import org.obiba.magma.views.View;
import org.obiba.magma.views.View.Builder;
import org.obiba.magma.views.WhereClause;
import org.obiba.magma.views.support.NoneClause;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Magma.VariableListViewDto;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.springframework.stereotype.Component;

/**
 * An implementation of {@Code ViewDtoExtension} for {@code View} instances that have a {@code ListClause}.
 */
@Component
public class VariableListViewDtoExtension implements ViewDtoExtension {

  @Override
  public boolean isExtensionOf(@NotNull ViewDto viewDto) {
    return viewDto.hasExtension(VariableListViewDto.view);
  }

  @Override
  public boolean isDtoOf(@NotNull View view) {
    return !(view.getListClause() instanceof NoneClause);
  }

  @Override
  public View fromDto(ViewDto viewDto, Builder viewBuilder) {
    VariableListViewDto listDto = viewDto.getExtension(VariableListViewDto.view);

    Collection<Variable> variables = new LinkedHashSet<>();
    for(VariableDto variableDto : listDto.getVariablesList()) {
      variables.add(Dtos.fromDto(variableDto));
    }

    VariablesClause listClause = new VariablesClause();
    listClause.setVariables(variables);
    viewBuilder.list(listClause);

    return viewBuilder.build();
  }

  @Override
  public ViewDto asDto(View view) {
    ViewDto.Builder viewDtoBuilder = ViewDto.newBuilder();
    viewDtoBuilder.setDatasourceName(view.getDatasource().getName());
    viewDtoBuilder.setName(view.getName());
    if(view.getWhereClause() instanceof JavascriptClause) {
      viewDtoBuilder.setWhere(((JavascriptClause) view.getWhereClause()).getScript());
    }
    setFromTables(view, viewDtoBuilder);

    VariableListViewDto.Builder listDtoBuilder = VariableListViewDto.newBuilder();
    for(Variable v : view.getVariables()) {
      listDtoBuilder.addVariables(Dtos.asDto(v));
    }
    viewDtoBuilder.setExtension(VariableListViewDto.view, listDtoBuilder.build());

    return viewDtoBuilder.build();
  }

  private void setFromTables(ValueTableWrapper view, ViewDto.Builder viewDtoBuilder) {
    ValueTable from = view.getWrappedValueTable();
    if(from instanceof JoinTable) {
      List<ValueTable> fromTables = ((JoinTable) from).getTables();
      for(ValueTable vt : fromTables) {
        viewDtoBuilder.addFrom(toStringReference(vt));
      }
    } else {
      viewDtoBuilder.addFrom(toStringReference(from));
    }
  }

  String toStringReference(ValueTable vt) {
    if(vt instanceof ValueTableReference) {
      return ((ValueTableReference) vt).getReference();
    }
    return vt.getDatasource().getName() + "." + vt.getName();
  }

  @Override
  public TableDto asTableDto(ViewDto viewDto, Magma.TableDto.Builder tableDtoBuilder) {
    VariableListViewDto listDto = viewDto.getExtension(VariableListViewDto.view);
    if(listDto.getVariablesCount() > 0) {
      tableDtoBuilder.setEntityType(listDto.getVariables(0).getEntityType());
    }
    tableDtoBuilder.addAllVariables(listDto.getVariablesList());
    return tableDtoBuilder.build();
  }

}