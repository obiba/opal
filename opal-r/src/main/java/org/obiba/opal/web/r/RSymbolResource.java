/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.r;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.r.service.OpalRSession;

public interface RSymbolResource {

  void setName(String name);

  void setOpalRSession(OpalRSession rSession);

  void setIdentifiersTableService(IdentifiersTableService identifiersTableService);

  String getName();

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Response getSymbol();

  @PUT
  @Consumes(MediaType.TEXT_PLAIN)
  Response putString(@Context UriInfo uri, String content, @QueryParam("async") @DefaultValue("false") boolean async);

  @PUT
  @Consumes("application/x-rscript")
  Response putRScript(@Context UriInfo uri, String script, @QueryParam("async") @DefaultValue("false") boolean async);

  @PUT
  @Consumes("application/x-opal")
  Response putMagma(@Context UriInfo uri, String path, @QueryParam("variables") String variableFilter,
      @QueryParam("missings") @DefaultValue("false") Boolean missings, @QueryParam("identifiers") String identifiers,
      @QueryParam("async") @DefaultValue("false") boolean async);

  @DELETE
  Response rm();
}
