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

import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableUpdateListener;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
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

@Component("variableViewResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class VariableViewResourceImpl extends VariableResourceImpl implements VariableViewResource {

  @Autowired
  private ViewManager viewManager;

  @Autowired
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private Collection<ValueTableUpdateListener> tableListeners;

  @Override
  public VariableDto get(UriInfo uriInfo) {
    UriBuilder uriBuilder = UriBuilder.fromPath("/");
    List<PathSegment> pathSegments = uriInfo.getPathSegments();
    for(int i = 0; i < 4; i++) {
      uriBuilder.segment(pathSegments.get(i).getPath());
    }
    String tableUri = uriBuilder.build().toString();
    Magma.LinkDto linkDto = Magma.LinkDto.newBuilder().setLink(tableUri).setRel(getValueTable().getName()).build();

    return Dtos.asDto(linkDto, getValueTable().getVariable(getName())).build();
  }

  @Override
  public Response createOrUpdateVariable(VariableDto variableDto, @Nullable String comment) {
    // The variable must exist
    ValueTable table = getValueTable();
    Variable variable = table.getVariable(getName());

    if(!variable.getEntityType().equals(variableDto.getEntityType())) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    View view = getValueTableAsView();

    try(ValueTableWriter.VariableWriter variableWriter = view.getListClause().createWriter()) {

      // Rename existing variable
      if(!variableDto.getName().equals(variable.getName())) {
        renameVariable(variable, variableDto.getName(), table, variableWriter);
      }
      variableWriter.writeVariable(Dtos.fromDto(variableDto));
      viewManager.addView(getDatasource().getName(), view, comment);
    }
    return Response.ok().build();
  }

  private void renameVariable(Variable variable, String newName, ValueTable table,
      ValueTableWriter.VariableWriter variableWriter) {
    if(tableListeners != null && !tableListeners.isEmpty()) {
      for(ValueTableUpdateListener listener : tableListeners) {
        listener.onRename(table, variable, newName);
      }
    }
    variableWriter.removeVariable(variable);
  }

  @Override
  public Response deleteVariable() {
    View view = getValueTableAsView();
    try(ValueTableWriter.VariableWriter variableWriter = view.getListClause().createWriter()) {

      // Remove from listClause
      for(VariableValueSource v : view.getListClause().getVariableValueSources()) {
        if(v.getVariable().getName().equals(getName())) {
          variableWriter.removeVariable(v.getVariable());
          viewManager.addView(getDatasource().getName(), view, "Remove " + getName());
          break;
        }
      }
    }

    return Response.ok().build();
  }

  private View getValueTableAsView() {
    return viewManager.getView(getDatasource().getName(), getValueTable().getName());
  }
}
