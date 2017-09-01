/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.obiba.opal.web.ws.security.AuthorizeResource;

import javax.annotation.Nullable;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public interface VariablesResource {

  void setValueTable(ValueTable valueTable);

  void setLocales(Set<Locale> locales);

  /**
   * Get a chunk of variables, optionally filtered by a script
   *
   * @param uriInfo
   * @param script  script for filtering the variables
   * @param offset
   * @param limit
   * @return
   */
  @GET
  Iterable<Magma.VariableDto> getVariables(@Context Request request, @Context UriInfo uriInfo, @QueryParam("script") String script,
                                           @QueryParam("offset") @DefaultValue("0") Integer offset, @Nullable @QueryParam("limit") Integer limit);

  @GET
  @Path("/excel")
  @Produces("application/vnd.ms-excel")
  @AuthenticatedByCookie
  @AuthorizeResource
  Response getExcelDictionary(@Context Request request) throws MagmaRuntimeException, IOException;

  @PUT
  @Path("/_order")
  Response setVariableOrder(@QueryParam("variable") List<String> variables);

  /**
   * Batch edition of an attribute in all the specified variables.
   *
   * @param namespace
   * @param name
   * @param locale
   * @param value If null or empty, the attribute is removed.
   * @param variables
   * @return
   */
  @PUT
  @Path("/_attribute")
  Response updateAttribute(@QueryParam("namespace") String namespace, @QueryParam("name") String name,
                        @QueryParam("locale") String locale, @QueryParam("value") String value,
                        @FormParam("variable") List<String> variables);

  @POST
  Response addOrUpdateVariables(List<Magma.VariableDto> variables, @Nullable @QueryParam("comment") String comment);

  @DELETE
  Response deleteVariables(@QueryParam("variable") List<String> variables);

  @Path("/locales")
  LocalesResource getLocalesResource();
}
