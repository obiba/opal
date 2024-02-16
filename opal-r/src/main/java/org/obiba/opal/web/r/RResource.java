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

import com.google.common.base.Strings;
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.spi.r.ROperationWithResult;
import org.obiba.opal.spi.r.RScriptROperation;
import org.obiba.opal.spi.r.RSerialize;
import org.obiba.opal.spi.r.RServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Handles web services on the current R session of the invoking Opal user. A current R session must be defined,
 * otherwise the web service calls will fail with a 404 status.
 */
@Component
@Transactional
@Path("/r")
public class RResource {

  private static final Logger log = LoggerFactory.getLogger(RResource.class);

  @Autowired
  private RServerManagerService rServerManagerService;

  @Autowired
  private ApplicationContext applicationContext;

  @POST
  @Path("/execute")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response executeBinary(@QueryParam("script") String script, @QueryParam("profile") String profile, String body) throws RServerException {
    return execute(profile, script, body, RSerialize.RAW);
  }

  @POST
  @Path("/execute")
  @Produces(MediaType.APPLICATION_JSON)
  public Response executeJSON(@QueryParam("script") String script, @QueryParam("profile") String profile, String body) throws RServerException {
    return execute(profile, script, body, RSerialize.JSON);
  }

  public Response execute(String profile, String script, String body, RSerialize serialize) throws RServerException {
    String rScript = script;
    if (Strings.isNullOrEmpty(rScript)) {
      rScript = body;
    }

    if (Strings.isNullOrEmpty(rScript)) return Response.status(Status.BAD_REQUEST).build();

    ROperationWithResult rop = new RScriptROperation(rScript, serialize);
    rServerManagerService.getRServer(profile).execute(rop);
    if (rop.hasResult()) {
      if (rop.getResult().isRaw())
        return Response.ok().entity(rop.getResult().asBytes()).type(MediaType.APPLICATION_OCTET_STREAM).build();
      else
        return Response.ok().entity(rop.getResult().asJSON()).type(MediaType.APPLICATION_JSON).build();
    } else {
      log.error("R Script '{}' has result: {}, has raw result: {}", rScript, rop.hasResult(), rop.hasResult() && rop.getResult().isRaw());
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("/sessions")
  public RSessionsResource getSessionsResource() {
    RSessionsResource resource = applicationContext
        .getBean("opalRSessionsResource", RSessionsResource.class);
    return resource;
  }

}
