/*******************************************************************************
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.util.Locale;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;

import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.lang.Closeables;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma.VariableDto;

public class VariableViewResource extends VariablesViewResource {

  private final String name;

  public VariableViewResource(ViewManager viewManager, ViewDtos viewDtos, ValueTable valueTable, Set<Locale> locales,
      String name) {
    super(viewManager, viewDtos, valueTable, locales);
    this.name = name;
  }

  @PUT
  public Response createOrUpdateVariable(VariableDto variable) {
    ValueTableWriter.VariableWriter vw = null;
    try {
      // The variable must exist
      Variable v = getValueTable().getVariable(name);

      if(!v.getEntityType().equals(variable.getEntityType())) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }

      View view = getValueTableAsView();
      vw = view.getListClause().createWriter();

      // Rename existing variable
      if(!variable.getName().equals(v.getName())) {
        // TODO: Uncomment once ValueTableWriter implements removeVariable();
        // vw.removeVariable(v);
      }
      vw.writeVariable(Dtos.fromDto(variable));
      viewManager.addView(getDatasource().getName(), view);

    } catch(NoSuchVariableException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    } finally {
      Closeables.closeQuietly(vw);
    }
    return Response.ok().build();
  }

  @DELETE
  public Response deleteVariable() {
    // TODO: Uncomment once ValueTableWriter implements removeVariable();
//    ValueTableWriter.VariableWriter vw = null;
//    try {
//      View view = getValueTableAsView();
//      vw = view.getListClause().createWriter();
//
//      // Remove from listClause
//      for (VariableValueSource v: view.getListClause().getVariableValueSources()){
//        if (v.getVariable().getName().equals(name)){
//          vw.removeVariable(v);
//          break;
//        }
//      }
//
//    } finally {
//      Closeables.closeQuietly(vw);
//    }

    return Response.ok().build();
  }
}
