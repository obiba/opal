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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.lang.Closeables;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.web.magma.view.ViewDtos;
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

  @Autowired
  private ViewManager viewManager;

  private ViewDtos viewDtos;

  @Override
  public void setViewDtos(ViewDtos viewDtos) {
    this.viewDtos = viewDtos;
  }

  @Override
  public Response addOrUpdateVariables(List<VariableDto> variables, @Nullable String comment) {
    try {
      if(!getValueTable().isView()) {
        addOrUpdateTableVariables(variables);
      } else if(viewManager != null || viewDtos == null) {
        addOrUpdateViewVariables(variables, comment);
      } else {
        return Response.status(Status.BAD_REQUEST).entity(getErrorMessage(Status.BAD_REQUEST, "CannotWriteToView"))
            .build();
      }

      return Response.ok().build();
    } catch(Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(getErrorMessage(Status.INTERNAL_SERVER_ERROR, e.toString())).build();
    }
  }

  private void addOrUpdateViewVariables(Iterable<VariableDto> variables, @Nullable String comment) {
    VariableWriter vw = null;
    try {
      View view = getValueTableAsView();
      vw = view.getListClause().createWriter();
      for(VariableDto variable : variables) {
        vw.writeVariable(Dtos.fromDto(variable));
      }
      viewManager.addView(getDatasource().getName(), view, comment);
    } finally {
      Closeables.closeQuietly(vw);
    }
  }

  @Override
  public Response deleteVariables(List<String> variables) {
    ValueTableWriter.VariableWriter vw = null;
    try {
      View view = getValueTableAsView();
      vw = view.getListClause().createWriter();

      // Remove from listClause
      for(VariableValueSource v : view.getListClause().getVariableValueSources()) {
        if(variables.contains(v.getVariable().getName())) {
          vw.removeVariable(v.getVariable());
          viewManager.addView(getDatasource().getName(), view, "Remove " + v.getVariable().getName());
        }
      }

    } finally {
      Closeables.closeQuietly(vw);
    }

    return Response.ok().build();
  }

  private View getValueTableAsView() {
    return viewManager.getView(getDatasource().getName(), getValueTable().getName());
  }

}
