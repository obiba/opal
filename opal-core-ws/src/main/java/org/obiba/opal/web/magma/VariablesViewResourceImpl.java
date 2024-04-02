/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import com.google.common.base.Joiner;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.ValueView;
import org.obiba.magma.Variable;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.magma.views.support.VariableOperationContext;
import org.obiba.opal.core.event.VariableDeletedEvent;
import org.obiba.opal.core.event.VariablesUpdatedEvent;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nullable;
import javax.validation.constraints.NotNull;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component("variablesViewResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class VariablesViewResourceImpl extends VariablesResourceImpl implements VariablesViewResource {

  @NotNull
  @Autowired
  private ViewManager viewManager;

  private ViewDtos viewDtos;

  @Autowired
  public void setViewDtos(ViewDtos viewDtos) {
    this.viewDtos = viewDtos;
  }

  @Override
  public Response addOrUpdateVariables(List<VariableDto> variables, @Nullable String comment) {
    addOrUpdateViewVariables(variables, comment);
    return Response.ok().build();
  }

  @Override
  public Response addOrUpdateVariablesFromFile(Magma.ViewDto viewDto, @Nullable String comment) {
    ValueView view = viewDtos.fromDto(viewDto);
    Initialisables.initialise(view);
    ValueView source = getValueTableAsView();
    VariableOperationContext operationContext = new VariableOperationContext();
    try (VariableWriter variableWriter = source.createVariableWriter()) {
      for (Variable variable : view.getVariables()) {
        operationContext.addVariable(source, variable);
        variableWriter.writeVariable(variable);
      }
      viewManager.addView(getDatasource().getName(), source, comment, operationContext);
    }

    return Response.ok().build();
  }

  @Override
  void addOrUpdateTableVariables(Iterable<Variable> variables) {
    addOrUpdateViewVariables(variables, "Update");
  }

  private void addOrUpdateViewVariables(List<VariableDto> variables, @Nullable String comment) {
    addOrUpdateViewVariables(variables.stream().map(Dtos::fromDto).collect(Collectors.toList()), comment);
  }

  private void addOrUpdateViewVariables(Iterable<Variable> variables, @Nullable String comment) {
    ValueView view = getValueTableAsView();
    VariableOperationContext operationContext = new VariableOperationContext();
    try (VariableWriter variableWriter = view.createVariableWriter()) {
      for (Variable variable : variables) {
        operationContext.addVariable(view, variable);
        variableWriter.writeVariable(variable);
      }
      viewManager.addView(getDatasource().getName(), view, comment, operationContext);
    }
    getEventBus().post(new VariablesUpdatedEvent(getValueTable(), variables));
  }

  @Override
  public Response deleteVariables(List<String> variableNames) {
    ValueView view = getValueTableAsView();
    VariableOperationContext operationContext = new VariableOperationContext();
    try (ValueTableWriter.VariableWriter variableWriter = view.createVariableWriter()) {
      List<String> names = new ArrayList<>();
      // Remove from listClause
      for (Variable variable : view.getVariables()) {
        String name = variable.getName();
        if (variableNames.contains(name)) {
          getEventBus().post(new VariableDeletedEvent(getValueTable(), variable));
          operationContext.deleteVariable(view, variable);
          names.add(name);
          variableWriter.removeVariable(variable);
        }
      }

      viewManager.addView(getDatasource().getName(), view, "Remove " + Joiner.on(',').join(names), operationContext);
    }
    return Response.ok().build();
  }

  private ValueView getValueTableAsView() {
    return viewManager.getView(getDatasource().getName(), getValueTable().getName());
  }
}
