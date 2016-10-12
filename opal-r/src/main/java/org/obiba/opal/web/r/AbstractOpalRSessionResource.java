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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obiba.opal.r.RASyncOperationTemplate;
import org.obiba.opal.r.ROperationTemplate;
import org.obiba.opal.r.ROperationWithResult;
import org.obiba.opal.r.RScriptROperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for handling current R session related web services.
 */
public abstract class AbstractOpalRSessionResource {

  private static final Logger log = LoggerFactory.getLogger(AbstractOpalRSessionResource.class);

  /**
   * Executes a R script and set the REXP result in its serialized form in the Response.
   *
   * @param script
   * @return
   */
  Response executeScript(ROperationTemplate rSession, String script, boolean async) {
    if(script == null) return Response.status(Status.BAD_REQUEST).build();

    ROperationWithResult rop = new RScriptROperation(script);
    if(async && rSession instanceof RASyncOperationTemplate) {
      String id = ((RASyncOperationTemplate)rSession).executeAsync(rop);
      return Response.ok().entity(id).type(MediaType.TEXT_PLAIN_TYPE).build();
    } else {
      rSession.execute(rop);
      if(rop.hasResult() && rop.hasRawResult()) {
        return Response.ok().entity(rop.getRawResult().asBytes()).build();
      }
      log.error("R Script '{}' has result: {}, has raw result: {}", script, rop.hasResult(), rop.hasRawResult());
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  Response executeScript(ROperationTemplate rSession, String script) {
    return executeScript(rSession, script, false);
  }

}
