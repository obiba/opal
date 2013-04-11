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
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.service.ImportService;
import org.obiba.opal.search.StatsIndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.obiba.opal.web.model.Ws;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

public class ViewResource extends AbstractValueTableResource {

  private final ViewManager viewManager;

  private final ViewDtos viewDtos;

  private final ImportService importService;

  private final OpalSearchService opalSearchService;

  private final StatsIndexManager statsIndexManager;

  private final ElasticSearchProvider esProvider;

  public ViewResource(ViewManager viewManager, View view, ViewDtos viewDtos, Set<Locale> locales,
      ImportService importService, OpalSearchService opalSearchService, StatsIndexManager statsIndexManager,
      ElasticSearchProvider esProvider) {
    super(view, locales);
    this.viewDtos = viewDtos;
    this.viewManager = viewManager;
    this.importService = importService;
    this.opalSearchService = opalSearchService;
    this.statsIndexManager = statsIndexManager;
    this.esProvider = esProvider;
  }

  public ViewResource(ViewManager viewManager, View view, ViewDtos viewDtos, OpalSearchService opalSearchService,
      StatsIndexManager statsIndexManager, ElasticSearchProvider esProvider, ImportService importService) {
    this(viewManager, view, viewDtos, Collections.<Locale>emptySet(), importService, opalSearchService,
        statsIndexManager, esProvider);
  }

  @GET
  public ViewDto getView() {
    return viewDtos.asDto(asView());
  }

  @PUT
  public Response updateView(ViewDto viewDto) {
    if(!viewDto.hasName()) return Response.status(Status.BAD_REQUEST).build();
    if(!viewDto.getName().equals(getValueTable().getName())) return Response.status(Status.BAD_REQUEST).build();

    // Validate that all variable entity types are the same as the base value table
    Magma.VariableListViewDto derivedVariables = viewDto.getExtension(Magma.VariableListViewDto.view);

    for(Magma.VariableDto variable : derivedVariables.getVariablesList()) {
      if(!variable.getEntityType().equals(getValueTable().getEntityType())) {

        Collection<String> args = new ArrayList<String>();
        args.add(variable.getEntityType());
        args.add(getValueTable().getEntityType());

        return Response.status(Response.Status.BAD_REQUEST).entity(
            Ws.ClientErrorDto.newBuilder().setCode(Status.BAD_REQUEST.getStatusCode())
                .setStatus("CopyVariableIncompatibleEntityType").addAllArguments(args).build()).build();

      }
    }
    viewManager.addView(getDatasource().getName(), viewDtos.fromDto(viewDto));

    return Response.ok().build();
  }

  @Path("/variables")
  public VariablesViewResource getVariables(@Context Request request) {
    return new VariablesViewResource(viewManager, viewDtos, getValueTable(), getLocales());
  }

  @DELETE
  public Response removeView() {
    viewManager.removeView(getDatasource().getName(), getValueTable().getName());
    return Response.ok().build();
  }

  @GET
  @Path("/xml")
  @Produces("application/xml")
  public Response downloadViewDefinition() {
    return Response.ok(asView(), "application/xml")
        .header("Content-Disposition", "attachment; filename=\"" + getValueTable().getName() + ".xml\"").build();
  }

  @Path("/from")
  @Bean
  @Scope("request")
  public TableResource getFrom() {
    return new TableResource(asView().getWrappedValueTable(), getLocales(), importService, opalSearchService,
        statsIndexManager, esProvider);
  }

  @Override
  @Path("/locales")
  public LocalesResource getLocalesResource() {
    return super.getLocalesResource();
  }

  protected View asView() {
    return (View) getValueTable();
  }

}
