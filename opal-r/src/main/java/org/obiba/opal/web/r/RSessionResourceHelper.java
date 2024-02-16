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

import org.obiba.opal.spi.r.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

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
  static Response executeScript(ROperationTemplate ropTemplate, String script, boolean async, RSerialize serialize) {
    if (script == null) return Response.status(Status.BAD_REQUEST).build();
    return executeScript(ropTemplate, new RScriptROperation(script, serialize), async);
  }

  static Response executeScript(ROperationTemplate ropTemplate, ROperationWithResult rop, boolean async) {
    if (async && ropTemplate instanceof RASyncOperationTemplate) {
      String id = ((RASyncOperationTemplate) ropTemplate).executeAsync(rop);
      return Response.ok().entity(id).type(MediaType.TEXT_PLAIN_TYPE).build();
    } else {
      ropTemplate.execute(rop);
      if (rop.hasResult()) {
        if (rop.getResult().isRaw())
          return Response.ok().entity(rop.getResult().asBytes()).type(MediaType.APPLICATION_OCTET_STREAM).build();
        else
          return Response.ok().entity(rop.getResult().asJSON()).type(MediaType.APPLICATION_JSON).build();
      }
      log.error("R Script '{}' has result: {}, has raw result: {}", rop, rop.hasResult(), rop.hasResult() && rop.getResult().isRaw());
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  static Response executeScript(ROperationTemplate ropTemplate, String script, RSerialize serialize) {
    return executeScript(ropTemplate, script, false, serialize);
  }

}
