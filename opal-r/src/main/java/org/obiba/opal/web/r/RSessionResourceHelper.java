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

import org.obiba.opal.r.RASyncOperationTemplate;
import org.obiba.opal.r.ROperationTemplate;
import org.obiba.opal.r.ROperationWithResult;
import org.obiba.opal.r.RScriptROperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Base class for handling current R session related web services.
 */
class RSessionResourceHelper {

  private static final Logger log = LoggerFactory.getLogger(RSessionResourceHelper.class);

  /**
   * Executes a R script and set the REXP result in its serialized form in the Response.
   *
   * @param script
   * @return
   */
  static Response executeScript(ROperationTemplate ropTemplate, String script, boolean async) {
    if(script == null) return Response.status(Status.BAD_REQUEST).build();

    ROperationWithResult rop = new RScriptROperation(script);
    if(async && ropTemplate instanceof RASyncOperationTemplate) {
      String id = ((RASyncOperationTemplate)ropTemplate).executeAsync(rop);
      return Response.ok().entity(id).type(MediaType.TEXT_PLAIN_TYPE).build();
    } else {
      ropTemplate.execute(rop);
      if(rop.hasResult() && rop.hasRawResult()) {
        return Response.ok().entity(rop.getRawResult().asBytes()).build();
      }
      log.error("R Script '{}' has result: {}, has raw result: {}", script, rop.hasResult(), rop.hasRawResult());
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  static Response executeScript(ROperationTemplate rSession, String script) {
    return executeScript(rSession, script, false);
  }

}
