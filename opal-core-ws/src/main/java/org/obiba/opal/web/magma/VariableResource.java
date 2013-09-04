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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.cache.Cache;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.lang.Closeables;
import org.obiba.opal.core.domain.VariableNature;
import org.obiba.opal.search.StatsIndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.math.AbstractSummaryResource;
import org.obiba.opal.web.magma.math.CategoricalSummaryResource;
import org.obiba.opal.web.magma.math.ContinuousSummaryResource;
import org.obiba.opal.web.magma.math.DefaultSummaryResource;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.VariableDto;

public class VariableResource {

  private final ValueTable valueTable;

  private final VariableValueSource vvs;

  private final OpalSearchService opalSearchService;

  private final StatsIndexManager statsIndexManager;

  private final ElasticSearchProvider esProvider;

  private final String name;

  public VariableResource(ValueTable valueTable, VariableValueSource vvs, OpalSearchService opalSearchService,
      StatsIndexManager statsIndexManager, ElasticSearchProvider esProvider, String name) {
    this.valueTable = valueTable;
    this.vvs = vvs;
    this.opalSearchService = opalSearchService;
    this.statsIndexManager = statsIndexManager;
    this.esProvider = esProvider;
    this.name = name;
  }

  @GET
  public VariableDto get(@Context UriInfo uriInfo) {
    UriBuilder uriBuilder = UriBuilder.fromPath("/");
    List<PathSegment> pathSegments = uriInfo.getPathSegments();
    for(int i = 0; i < 4; i++) {
      uriBuilder.segment(pathSegments.get(i).getPath());
    }
    String tableUri = uriBuilder.build().toString();
    Magma.LinkDto linkDto = Magma.LinkDto.newBuilder().setLink(tableUri).setRel(valueTable.getName()).build();
    return Dtos.asDto(linkDto, vvs.getVariable()).build();
  }

  @PUT
  public Response updateVariable(VariableDto variable) {

    ValueTableWriter vtw = null;
    ValueTableWriter.VariableWriter vw = null;
    try {
      // The variable must exist
      Variable v = getValueTable().getVariable(name);

      if(!v.getEntityType().equals(variable.getEntityType()) || !v.getName().equals(variable.getName())) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }

      vtw = getValueTable().getDatasource().createWriter(getValueTable().getName(), getValueTable().getEntityType());

      vw = vtw.writeVariables();
      vw.writeVariable(Dtos.fromDto(variable));

      return Response.ok().build();

    } catch(NoSuchVariableException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    } finally {
      Closeables.closeQuietly(vw);
      Closeables.closeQuietly(vtw);
    }
  }

  @DELETE
  public Response deleteVariable(VariableDto variable) {
    // TODO: Uncomment once ValueTableWriter implements removeVariable();
//    ValueTableWriter vtw = null;
//    ValueTableWriter.VariableWriter vw = null;
//    try {
//      // The variable must exist
//      Variable v = getValueTable().getVariable(variable.getName());
//
//      if(!v.getEntityType().equals(variable.getEntityType())) {
//        return Response.status(Response.Status.BAD_REQUEST).build();
//      }
//
//      vtw = getValueTable().getDatasource().createWriter(getValueTable().getName(), getValueTable().getEntityType());
//
//      vw = vtw.writeVariables();
//      vw.removeVariable(Dtos.fromDto(variable));
//
//      return Response.ok().build();
//
//    } catch(NoSuchVariableException e) {
//      return Response.status(Response.Status.NOT_FOUND).build();
//    } finally {
//      Closeables.closeQuietly(vw);
//      Closeables.closeQuietly(vtw);
//    }
    return Response.ok().build();
  }

  @Path("/summary")
  @Cache(isPrivate = true, mustRevalidate = true, maxAge = 0)
  public AbstractSummaryResource getSummary(@Context Request request, @QueryParam("nature") String natureStr) {

    TimestampedResponses.evaluate(request, getValueTable());

    Variable variable = vvs.getVariable();
    VariableNature nature = natureStr == null
        ? VariableNature.getNature(variable)
        : VariableNature.valueOf(natureStr.toUpperCase());

    switch(nature) {
      case CATEGORICAL:
        return new CategoricalSummaryResource(opalSearchService, statsIndexManager, esProvider, getValueTable(),
            variable, vvs);
      case CONTINUOUS:
        return new ContinuousSummaryResource(opalSearchService, statsIndexManager, esProvider, getValueTable(),
            variable, vvs);
      case TEMPORAL:
      case UNDETERMINED:
      default:
        return new DefaultSummaryResource(opalSearchService, statsIndexManager, esProvider, getValueTable(), variable,
            vvs);
    }
  }

  VariableValueSource getVariableValueSource() {
    return vvs;
  }

  protected ValueTable getValueTable() {
    return valueTable;
  }

}
