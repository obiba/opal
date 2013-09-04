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
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.lang.Closeables;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma.VariableDto;

public class VariablesViewResource extends VariablesResource {

  protected final ViewManager viewManager;

  protected final ViewDtos viewDtos;

  public VariablesViewResource(ViewManager viewManager, ViewDtos viewDtos, ValueTable valueTable, Set<Locale> locales) {
    super(valueTable, locales);
    this.viewManager = viewManager;
    this.viewDtos = viewDtos;
  }

  @Override
  @POST
  public Response addOrUpdateVariables(List<VariableDto> variables) {
    try {

      // @TODO Check if table can be modified and respond with "IllegalTableModification" (it seems like this cannot be
      // done with the current Magma implementation).

      if(!getValueTable().isView()) {
        addOrUpdateTableVariables(variables);
      } else if(viewManager != null || viewDtos == null) {
        addOrUpdateViewVariables(variables);
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

  private void addOrUpdateViewVariables(Iterable<VariableDto> variables) {
    VariableWriter vw = null;
    try {
      View view = getValueTableAsView();
      vw = view.getListClause().createWriter();
      for(VariableDto variable : variables) {
        vw.writeVariable(Dtos.fromDto(variable));
      }
      viewManager.addView(getDatasource().getName(), view);
    } finally {
      Closeables.closeQuietly(vw);
    }
  }

  //
  // private methods
  //

  protected View getValueTableAsView() {
    return viewManager.getView(getDatasource().getName(), getValueTable().getName());
  }

}
