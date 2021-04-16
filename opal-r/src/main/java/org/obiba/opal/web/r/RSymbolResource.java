/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.r;

import org.obiba.opal.core.service.DataExportService;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.service.ResourceReferenceService;
import org.obiba.opal.r.service.RServerSession;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public interface RSymbolResource {

  void setName(String name);

  void setRServerSession(RServerSession rSession);

  void setIdentifiersTableService(IdentifiersTableService identifiersTableService);

  void setDataExportService(DataExportService dataExportService);

  void setResourceReferenceService(ResourceReferenceService resourceReferenceService);

  String getName();

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Response getSymbolBinary();

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  Response getSymbolJSON();

  @PUT
  @Consumes(MediaType.TEXT_PLAIN)
  Response putString(@Context UriInfo uri, String content, @QueryParam("async") @DefaultValue("false") boolean async);

  @PUT
  @Consumes("application/x-rscript")
  Response putRScript(@Context UriInfo uri, String script, @QueryParam("async") @DefaultValue("false") boolean async);

  /**
   * Assign value table variables to a R data.frame.
   *
   * @param uri
   * @param path
   * @param variableFilter
   * @param withMissings       Include values corresponding to "missing" categories.
   * @param idName             Include the entity ID as a column.
   * @param identifiersMapping Identifiers mapping to be used.
   * @param rClass             R data frame class (can be "data.frame" (default) or "tibble").
   * @param async
   * @return
   */
  @PUT
  @Consumes("application/x-opal")
  Response putMagma(@Context UriInfo uri, String path, @QueryParam("variables") String variableFilter,
                    @QueryParam("missings") @DefaultValue("false") Boolean withMissings, @QueryParam("id") String idName,
                    @QueryParam("identifiers") String identifiersMapping,
                    @QueryParam("class") @DefaultValue("data.frame") String rClass,
                    @QueryParam("async") @DefaultValue("false") boolean async);

  @PUT
  @Path("/table/{path}")
  Response putTable(@Context UriInfo uri, @PathParam("path") String path, @QueryParam("variables") String variableFilter,
                    @QueryParam("missings") @DefaultValue("false") Boolean withMissings, @QueryParam("id") String idName,
                    @QueryParam("identifiers") String identifiersMapping,
                    @QueryParam("class") @DefaultValue("data.frame") String rClass,
                    @QueryParam("async") @DefaultValue("false") boolean async);

  @PUT
  @Path("/resource/{path}")
  Response putResource(@Context UriInfo uri, @PathParam("path") String path,
                       @QueryParam("async") @DefaultValue("false") boolean async);

  @DELETE
  Response rm();

}
