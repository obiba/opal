/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.r;

import org.apache.commons.vfs2.FileSystemException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public interface OpalRSymbolResource extends RSymbolResource {

  /**
   * Push a R data object into the R server: content is expected to be the serialized form of the R object, base64 encoded.
   *
   * @param uri
   * @param content
   * @param async
   * @return
   */
  @POST
  @Consumes("application/x-rdata")
  Response putRData(@Context UriInfo uri, String content, @QueryParam("async") @DefaultValue("false") boolean async);

  /**
   * Import the R data associated to symbol into the given project.
   *
   * @param project
   * @return
   * @throws FileSystemException
   */
  @PUT
  @Path("/_import")
  Response importMagma(@QueryParam("project") String project);

  /**
   * Export a {@link org.obiba.magma.ValueTable} as R tibble.
   *
   * @param uri
   * @param path
   * @param variableFilter
   * @param idName
   * @param updatedName
   * @param identifiersMapping
   * @param destination
   * @return
   * @throws FileSystemException
   */
  @PUT
  @Path("/_export")
  Response exportMagma(@Context UriInfo uri, String path, @QueryParam("variables") String variableFilter,
                       @QueryParam("id") String idName, @QueryParam("updated") String updatedName,
                       @QueryParam("identifiers") String identifiersMapping,
                       @QueryParam("async") @DefaultValue("false") boolean async,
                       @QueryParam("destination") String destination);

}
