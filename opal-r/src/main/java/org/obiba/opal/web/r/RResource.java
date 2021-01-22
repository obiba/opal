/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.r;

import com.google.common.base.Strings;
import org.obiba.opal.r.service.RServerService;
import org.obiba.opal.spi.r.ROperationWithResult;
import org.obiba.opal.spi.r.RScriptROperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
  private RServerService rServerService;

  @Autowired
  private ApplicationContext applicationContext;

  @POST
  @Path("/execute")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response query(@QueryParam("script") String script, String body) {
    String rScript = script;
    if (Strings.isNullOrEmpty(rScript)) {
      rScript = body;
    }

    if (Strings.isNullOrEmpty(rScript)) return Response.status(Status.BAD_REQUEST).build();

    ROperationWithResult rop = new RScriptROperation(rScript);
    rServerService.execute(rop);
    if (rop.hasResult() && rop.getResult().isRaw()) {
      return Response.ok().entity(rop.getResult().asBytes()).build();
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
