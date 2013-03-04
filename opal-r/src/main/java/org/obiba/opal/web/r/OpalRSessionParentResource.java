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

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.obiba.opal.r.service.OpalRSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@Scope("request")
@Path("/r/session")
public class OpalRSessionParentResource {

  private final OpalRSessionManager opalRSessionManager;

  @Autowired
  public OpalRSessionParentResource(OpalRSessionManager opalRSessionManager) {
    this.opalRSessionManager = opalRSessionManager;
  }

  @Path("/{id}")
  public OpalRSessionResource getOpalRSessionResource(@PathParam("id") String id) {
    return new OpalRSessionResource(opalRSessionManager, opalRSessionManager.getSubjectRSession(id));
  }

  @Path("/current")
  public OpalRSessionResource getCurrentOpalRSessionResource() {
    if(!opalRSessionManager.hasSubjectCurrentRSession()) {
      opalRSessionManager.newSubjectCurrentRSession();
    }
    return new OpalRSessionResource(opalRSessionManager, opalRSessionManager.getSubjectCurrentRSession());
  }

}
