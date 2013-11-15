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

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obiba.magma.ValueTable;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.service.ImportService;
import org.obiba.opal.core.service.VariableStatsService;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.obiba.opal.web.support.InvalidRequestException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import edu.umd.cs.findbugs.annotations.Nullable;

public class ViewResource extends AbstractValueTableResource {

  private final ViewManager viewManager;

  private final ViewDtos viewDtos;

  private final ImportService importService;

  private final VariableStatsService variableStatsService;

  public ViewResource(ViewManager viewManager, View view, ViewDtos viewDtos, Set<Locale> locales,
      ImportService importService, VariableStatsService variableStatsService) {
    super(view, locales);
    this.viewDtos = viewDtos;
    this.viewManager = viewManager;
    this.importService = importService;
    this.variableStatsService = variableStatsService;
  }

  public ViewResource(ViewManager viewManager, View view, ViewDtos viewDtos, ImportService importService,
      VariableStatsService variableStatsService) {
    this(viewManager, view, viewDtos, Collections.<Locale>emptySet(), importService, variableStatsService);
  }

  @GET
  public ViewDto getView() {
    return viewDtos.asDto(asView());
  }

  @PUT
  public Response updateView(ViewDto viewDto, @Nullable @QueryParam("comment") String comment) {
    if(!viewDto.hasName()) return Response.status(Status.BAD_REQUEST).build();

    viewManager.addView(getDatasource().getName(), viewDtos.fromDto(viewDto), comment);
    if(!viewDto.getName().equals(getValueTable().getName())) {
      viewManager.removeView(getDatasource().getName(), getValueTable().getName());
    }

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
    return new TableResource(asView().getWrappedValueTable(), getLocales(), importService, variableStatsService);
  }

  @Override
  @Path("/locales")
  public LocalesResource getLocalesResource() {
    return super.getLocalesResource();
  }

  /**
   * Get variable resource.
   *
   * @param request
   * @param name
   * @return
   */
  @Path("/variable/{variable}")
  public VariableViewResource getVariable(@Context Request request, @PathParam("variable") String name) {
    return new VariableViewResource(viewManager, getValueTable(), getLocales(), name);
  }

  protected View asView() {
    ValueTable table = getValueTable();
    if (table.isView()) return (View)table;
    throw new InvalidRequestException("Not a view");
  }

}
