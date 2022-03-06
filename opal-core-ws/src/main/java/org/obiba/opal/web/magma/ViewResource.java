/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma;

import javax.annotation.Nullable;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.web.model.Magma;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

public interface ViewResource extends TableResource {

  @GET
  Magma.ViewDto getView();

  @PUT
  Response updateView(Magma.ViewDto viewDto, @Nullable @QueryParam("comment") String comment);

  @DELETE
  Response removeView();

  @PUT
  @Path("/_init")
  Response initView();

  @GET
  @Path("/xml")
  @Produces("application/xml")
  Response downloadViewDefinition();

  @Path("/from")
  @Bean
  @Scope("request")
  TableResource getFrom();

  /**
   * Get variable resource.
   *
   * @param name
   * @return
   */
  @Path("/variable/{variable}")
  VariableViewResource getVariable(@PathParam("variable") String name);
}
