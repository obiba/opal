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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.obiba.magma.views.View;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

public class ViewResource extends AbstractValueTableResource {

  private ViewDtos viewDtos;

  public ViewResource(View view, ViewDtos viewDtos, Set<Locale> locales) {
    super(view, locales);
    this.viewDtos = viewDtos;
  }

  public ViewResource(View view, ViewDtos viewDtos) {
    this(view, viewDtos, Collections.<Locale> emptySet());
  }

  @GET
  public ViewDto getView() {
    return viewDtos.asDto(asView());
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

  protected View asView() {
    return (View) getValueTable();
  }
}
