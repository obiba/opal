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

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

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
   * Save the R symbol representing a tibble into a file in the R session with a file extension supported by the
   * R package haven (SPSS, SAS or Stata file).
   *
   * @param destination
   * @return
   */
  @PUT
  @Path("/_save")
  Response saveRData(@QueryParam("destination") String destination);
}
