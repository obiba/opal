/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.magma.views.support.VariableOperationContext;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;

import edu.umd.cs.findbugs.annotations.Nullable;

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
    if(getValueTable().isView()) {
      addOrUpdateViewVariables(variables, comment);
    } else {
      addOrUpdateTableVariables(variables);
    }
    return Response.ok().build();
  }

  @Override
  public Response addOrUpdateVariablesFromFile(Magma.ViewDto viewDto, @Nullable String comment) {
    View view = viewDtos.fromDto(viewDto);
    view.initialise();
    View source = getValueTableAsView();
    VariableOperationContext operationContext = new VariableOperationContext();
    try(VariableWriter variableWriter = source.getListClause().createWriter()) {
      for(Variable variable : view.getVariables()) {
        operationContext.addVariable(source, variable);
        variableWriter.writeVariable(variable);
      }
      viewManager.addView(getDatasource().getName(), source, comment, operationContext);
    }

    return Response.ok().build();
  }

  private void addOrUpdateViewVariables(Iterable<VariableDto> variables, @Nullable String comment) {
    View view = getValueTableAsView();
    VariableOperationContext operationContext = new VariableOperationContext();
    try(VariableWriter variableWriter = view.getListClause().createWriter()) {
      for(VariableDto variableDto : variables) {
        Variable variable = Dtos.fromDto(variableDto);
        operationContext.addVariable(view, variable);
        variableWriter.writeVariable(variable);
      }
      viewManager.addView(getDatasource().getName(), view, comment, operationContext);
    }
  }

  @Override
  public Response deleteVariables(List<String> variables) {
    View view = getValueTableAsView();
    VariableOperationContext operationContext = new VariableOperationContext();
    try(ValueTableWriter.VariableWriter variableWriter = view.getListClause().createWriter()) {
      List<String> names = new ArrayList<>();
      // Remove from listClause
      for(VariableValueSource variableSource : view.getListClause().getVariableValueSources()) {
        Variable variable = variableSource.getVariable();
        String name = variable.getName();
        if(variables.contains(name)) {
          operationContext.deleteVariable(view, variable);
          names.add(name);
          variableWriter.removeVariable(variable);
        }
      }

      viewManager.addView(getDatasource().getName(), view, "Remove " + Joiner.on(',').join(names), operationContext);
    }
    return Response.ok().build();
  }

  private View getValueTableAsView() {
    return viewManager.getView(getDatasource().getName(), getValueTable().getName());
  }
}
