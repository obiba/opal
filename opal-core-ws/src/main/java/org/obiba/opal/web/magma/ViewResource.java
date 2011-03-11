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
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obiba.magma.views.View;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

public class ViewResource extends AbstractValueTableResource {

  private final OpalRuntime opalRuntime;

  private ViewDtos viewDtos;

  public ViewResource(OpalRuntime opalRuntime, View view, ViewDtos viewDtos, Set<Locale> locales) {
    super(view, locales);
    this.viewDtos = viewDtos;
    this.opalRuntime = opalRuntime;
  }

  public ViewResource(OpalRuntime opalRuntime, View view, ViewDtos viewDtos) {
    this(opalRuntime, view, viewDtos, Collections.<Locale> emptySet());
  }

  @GET
  public ViewDto getView() {
    return viewDtos.asDto(asView());
  }

  @PUT
  public Response updateView(ViewDto viewDto) {
    if(!viewDto.hasName()) return Response.status(Status.BAD_REQUEST).build();
    if(!viewDto.getName().equals(getValueTable().getName())) return Response.status(Status.BAD_REQUEST).build();

    opalRuntime.getViewManager().addView(getDatasource().getName(), viewDtos.fromDto(viewDto));

    return Response.ok().build();
  }

  @DELETE
  public Response removeView() {
    opalRuntime.getViewManager().removeView(getDatasource().getName(), getValueTable().getName());

    return Response.ok().build();
  }

  @GET
  @Path("/xml")
  @Produces("application/xml")
  public Response downloadViewDefinition() {
    return Response.ok(asView(), "application/xml").header("Content-Disposition", "attachment; filename=\"" + getValueTable().getName() + ".xml\"").build();
  }

  @Path("/from")
  @Bean
  @Scope("request")
  public TableResource getFrom() {
    return new TableResource(asView().getWrappedValueTable(), getLocales());
  }

  @Path("/locales")
  public LocalesResource getLocalesResource() {
    return super.getLocalesResource();
  }

  protected View asView() {
    return (View) getValueTable();
  }

}
