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

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.cache.Cache;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableValueSource;
import org.obiba.opal.web.magma.math.SummaryResource;
import org.obiba.opal.web.model.Magma;

public interface VariableResource {

  void setName(String name);

  void setValueTable(ValueTable valueTable);

  void setVariableValueSource(VariableValueSource variableValueSource);

  @GET
  Magma.VariableDto get(@Context UriInfo uriInfo);

  @PUT
  Response updateVariable(Magma.VariableDto variable);

  @DELETE
  Response deleteVariable();

  @PUT
  @Path("/attribute/{name}")
  Response updateVariableAttribute(@PathParam("name") String name, @QueryParam("namespace") String namespace, @QueryParam("locale") String locale, @QueryParam("value") String value);

  @DELETE
  @Path("/attribute/{name}")
  Response deleteVariableAttribute(@PathParam("name") String name, @QueryParam("namespace") String namespace, @QueryParam("locale") String locale);

  @Path("/summary")
  @Cache(isPrivate = true, mustRevalidate = true, maxAge = 0)
  SummaryResource getSummary(@Context UriInfo uriInfo, @Context Request request, @QueryParam("nature") String natureStr);

  VariableValueSource getVariableValueSource();
}
