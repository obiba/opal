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

import org.apache.commons.vfs2.FileSystemException;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public interface OpalRSessionResource extends RSessionResource {

  @POST
  @Path("/execute")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Response executeBinary(@QueryParam("script") String script, @QueryParam("async") @DefaultValue("false") boolean async,
                   String body);

  @POST
  @Path("/execute")
  @Produces(MediaType.APPLICATION_JSON)
  Response executeJSON(@QueryParam("script") String script, @QueryParam("async") @DefaultValue("false") boolean async,
                         String body);


  /**
   * Push a file from the opal file system into the R session workspace.
   *
   * @param source
   * @param destination
   * @return
   */
  @PUT
  @Path("/file/_push")
  Response pushFile(@QueryParam("source") String source, @QueryParam("destination") String destination) throws FileSystemException;

  /**
   * Pull a file from the R session workspace to the opal file system.
   *
   * @param source
   * @param destination
   * @return
   */
  @PUT
  @Path("/file/_pull")
  Response pullFile(@QueryParam("source") String source, @QueryParam("destination") String destination) throws FileSystemException;

}
