/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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

  @Path("/summary")
  @Cache(isPrivate = true, mustRevalidate = true, maxAge = 0)
  SummaryResource getSummary(@Context Request request, @QueryParam("nature") String natureStr);

  VariableValueSource getVariableValueSource();
}
