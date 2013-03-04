/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.r;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obiba.opal.r.RScriptROperation;
import org.obiba.opal.r.service.OpalRService;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

/**
 * Handles web services on the current R session of the invoking Opal user. A current R session must be defined,
 * otherwise the web service calls will fail with a 404 status.
 */
@Component
@Path("/r")
public class OpalRResource {

  private static final Logger log = LoggerFactory.getLogger(OpalRResource.class);

  private final OpalRService opalRService;

  private final OpalRSessionManager opalRSessionManager;

  @Autowired
  public OpalRResource(OpalRService opalRService, OpalRSessionManager opalRSessionManager) {
    super();
    this.opalRService = opalRService;
    this.opalRSessionManager = opalRSessionManager;
  }

  @POST
  @Path("/execute")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response query(@QueryParam("script") String script, String body) {
    String rscript = script;
    if(Strings.isNullOrEmpty(rscript)) {
      rscript = body;
    }

    if(Strings.isNullOrEmpty(rscript)) return Response.status(Status.BAD_REQUEST).build();

    RScriptROperation rop = new RScriptROperation(rscript);
    opalRService.execute(rop);
    if(rop.hasResult() && rop.hasRawResult()) {
      return Response.ok().entity(rop.getRawResult().asBytes()).build();
    } else {
      log.error("R Script '{}' has result: {}, has raw result: {}",
          new Object[] { rscript, rop.hasResult(), rop.hasRawResult() });
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("/sessions")
  public OpalRSessionsResource getSessionsResource() {
    return new OpalRSessionsResource(opalRSessionManager);
  }

}
