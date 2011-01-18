/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.r;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obiba.opal.r.RScriptROperation;
import org.obiba.opal.r.service.NoSuchRSessionException;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for handling current R session related web services.
 */
public abstract class AbstractCurrentOpalRSessionResource {

  private static final Logger log = LoggerFactory.getLogger(AbstractCurrentOpalRSessionResource.class);

  protected abstract OpalRSessionManager getOpalRSessionManager();

  /**
   * Executes a R script and set the REXP result in its serialized form in the Response.
   * @param script
   * @return
   */
  protected Response executeScript(String script) {
    if(script == null) return Response.status(Status.BAD_REQUEST).build();

    RScriptROperation rop = new RScriptROperation(script);
    getOpalRSessionManager().execute(rop);
    if(rop.hasResult() && rop.hasRawResult()) {
      return Response.ok().entity(rop.getRawResult().asBytes()).build();
    } else {
      log.error("R Script '{}' has result: {}, has raw result: {}", new Object[] { script, rop.hasResult(), rop.hasRawResult() });
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Check the current R session is defined for the invoking Opal user.
   */
  protected void checkHasCurrentRSession() {
    if(!getOpalRSessionManager().hasSubjectCurrentRSession()) throw new NoSuchRSessionException();
  }

  /**
   * Get the current R session identifier for the invoking Opal user.
   * @return
   */
  protected String getCurrentRSessionId() {
    return getOpalRSessionManager().getSubjectCurrentRSessionId();
  }

}
