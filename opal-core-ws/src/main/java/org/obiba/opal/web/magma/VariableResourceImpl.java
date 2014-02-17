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

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.opal.core.domain.VariableNature;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.math.CategoricalSummaryResource;
import org.obiba.opal.web.magma.math.ContinuousSummaryResource;
import org.obiba.opal.web.magma.math.DefaultSummaryResource;
import org.obiba.opal.web.magma.math.SummaryResource;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.support.InvalidRequestException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("variableResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class VariableResourceImpl extends AbstractValueTableResource implements VariableResource {

  private String name;

  private VariableValueSource variableValueSource;

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setVariableValueSource(VariableValueSource variableValueSource) {
    this.variableValueSource = variableValueSource;
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
    return Dtos.asDto(linkDto, variableValueSource.getVariable()).build();
  }

  @Override
  public Response updateVariable(VariableDto dto) {
    if(getValueTable().isView()) throw new InvalidRequestException("Derived variable must be updated by the view");

    // The variable must exist
    Variable variable = getValueTable().getVariable(name);

    if(!variable.getEntityType().equals(dto.getEntityType())) {
      throw new InvalidRequestException("Variable entity type must be the same as the one of the table");
    }

    if(!variable.getName().equals(dto.getName())) {
      throw new InvalidRequestException("Variable cannot be renamed");
    }

    try(ValueTableWriter tableWriter = getValueTable().getDatasource()
        .createWriter(getValueTable().getName(), getValueTable().getEntityType());
        ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables()) {
      variableWriter.writeVariable(Dtos.fromDto(dto));
      return Response.ok().build();
    }
  }

  @Override
  public Response deleteVariable() {
    if(getValueTable().isView()) throw new InvalidRequestException("Derived variable must be deleted by the view");

    // The variable must exist
    Variable v = getValueTable().getVariable(name);

    try(ValueTableWriter tableWriter = getValueTable().getDatasource()
        .createWriter(getValueTable().getName(), getValueTable().getEntityType());
        ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables()) {
      variableWriter.removeVariable(v);
      return Response.ok().build();
    }
  }

  @Override
  public SummaryResource getSummary(Request request, String natureStr) {

    TimestampedResponses.evaluate(request, getValueTable());

    Variable variable = variableValueSource.getVariable();
    VariableNature nature = natureStr == null
        ? VariableNature.getNature(variable)
        : VariableNature.valueOf(natureStr.toUpperCase());

    SummaryResource resource = getSummaryResourceClass(nature);
    resource.setValueTable(getValueTable());
    resource.setVariable(variable);
    resource.setVariableValueSource(variableValueSource);
    return resource;

  }

  private SummaryResource getSummaryResourceClass(VariableNature nature) {
    switch(nature) {
      case CATEGORICAL:
        return applicationContext.getBean(CategoricalSummaryResource.class);
      case CONTINUOUS:
        return applicationContext.getBean(ContinuousSummaryResource.class);
      case TEMPORAL:
      case UNDETERMINED:
      case GEO:
      default:
        return applicationContext.getBean(DefaultSummaryResource.class);
    }
  }

  @Override
  public VariableValueSource getVariableValueSource() {
    return variableValueSource;
  }

  String getName() {
    return name;
  }
}
