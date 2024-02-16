/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.datashield;

import java.util.List;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.obiba.datashield.core.DSMethodType;
import org.obiba.opal.web.model.DataShield;

public interface DataShieldEnvironmentResource {

  void setMethodType(DSMethodType methodType);

  @GET
  @Path("/methods")
  List<DataShield.DataShieldMethodDto> getDataShieldMethods(@QueryParam("profile") String profile);

  @DELETE
  @Path("/methods")
  Response deleteDataShieldMethods(@QueryParam("name") List<String> names, @QueryParam("profile") String profile);

  @POST
  @Path("/methods")
  Response createDataShieldMethod(@Context UriInfo uri, @QueryParam("profile") String profile, DataShield.DataShieldMethodDto dto);

  @GET
  @Path("/method/{name}")
  Response getDataShieldMethod(@PathParam("name") String name, @QueryParam("profile") String profile);

  @PUT
  @Path("/method/{name}")
  Response updateDataShieldMethod(@PathParam("name") String name, @QueryParam("profile") String profile, DataShield.DataShieldMethodDto dto);

  @DELETE
  @Path("/method/{name}")
  Response deleteDataShieldMethod(@PathParam("name") String name, @QueryParam("profile") String profile);
}
