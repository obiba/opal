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

import java.util.List;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.lang.Closeables;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import edu.umd.cs.findbugs.annotations.Nullable;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class VariableViewResourceImpl extends AbstractValueTableResource implements VariableViewResource {

  @Autowired
  private ViewManager viewManager;

  private String name;

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public VariableDto get(UriInfo uriInfo) {
    UriBuilder uriBuilder = UriBuilder.fromPath("/");
    List<PathSegment> pathSegments = uriInfo.getPathSegments();
    for(int i = 0; i < 4; i++) {
      uriBuilder.segment(pathSegments.get(i).getPath());
    }
    String tableUri = uriBuilder.build().toString();
    Magma.LinkDto linkDto = Magma.LinkDto.newBuilder().setLink(tableUri).setRel(getValueTable().getName()).build();

    return Dtos.asDto(linkDto, getValueTable().getVariable(name)).build();
  }

  @Override
  public Response createOrUpdateVariable(VariableDto variable, @Nullable String comment) {
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
        vw.removeVariable(v);
      }
      vw.writeVariable(Dtos.fromDto(variable));
      viewManager.addView(getDatasource().getName(), view, comment);

    } catch(NoSuchVariableException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    } finally {
      Closeables.closeQuietly(vw);
    }
    return Response.ok().build();
  }

  @Override
  public Response deleteVariable() {
    ValueTableWriter.VariableWriter vw = null;
    try {
      View view = getValueTableAsView();
      vw = view.getListClause().createWriter();

      // Remove from listClause
      for(VariableValueSource v : view.getListClause().getVariableValueSources()) {
        if(v.getVariable().getName().equals(name)) {
          vw.removeVariable(v.getVariable());
          viewManager.addView(getDatasource().getName(), view, "Remove " + name);
          break;
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
