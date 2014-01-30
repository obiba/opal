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

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import edu.umd.cs.findbugs.annotations.Nullable;

@Component("variablesViewResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class VariablesViewResourceImpl extends VariablesResourceImpl implements VariablesViewResource {

  @NotNull
  @Autowired
  private ViewManager viewManager;

  @Override
  public Response addOrUpdateVariables(List<VariableDto> variables, @Nullable String comment) {
    if(getValueTable().isView()) {
      addOrUpdateViewVariables(variables, comment);
    } else {
      addOrUpdateTableVariables(variables);
    }
    return Response.ok().build();
  }

  private void addOrUpdateViewVariables(Iterable<VariableDto> variables, @Nullable String comment) {
    View view = getValueTableAsView();
    try(VariableWriter variableWriter = view.getListClause().createWriter()) {
      for(VariableDto variable : variables) {
        variableWriter.writeVariable(Dtos.fromDto(variable));
      }
      viewManager.addView(getDatasource().getName(), view, comment);
    }
  }

  @Override
  public Response deleteVariables(List<String> variables) {
    View view = getValueTableAsView();
    try(ValueTableWriter.VariableWriter variableWriter = view.getListClause().createWriter()) {
      // Remove from listClause
      for(VariableValueSource variableSource : view.getListClause().getVariableValueSources()) {
        String name = variableSource.getVariable().getName();
        if(variables.contains(name)) {
          variableWriter.removeVariable(variableSource.getVariable());
          viewManager.addView(getDatasource().getName(), view, "Remove " + name);
        }
      }
    }
    return Response.ok().build();
  }

  private View getValueTableAsView() {
    return viewManager.getView(getDatasource().getName(), getValueTable().getName());
  }

}
