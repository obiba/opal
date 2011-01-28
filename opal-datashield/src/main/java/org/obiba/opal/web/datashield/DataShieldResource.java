/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.datashield;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.r.service.OpalRService;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.r.OpalRSessionResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/datashield")
public class DataShieldResource {

  private final OpalRService opalRService;

  private final OpalRuntime opalRuntime;

  private final OpalRSessionManager opalRSessionManager;

  @Autowired
  public DataShieldResource(OpalRService opalRService, OpalRuntime opalRuntime, OpalRSessionManager opalRSessionManager) {
    if(opalRService == null) throw new IllegalArgumentException("opalRService cannot be null");
    if(opalRuntime == null) throw new IllegalArgumentException("opalRuntime cannot be null");
    if(opalRSessionManager == null) throw new IllegalArgumentException("opalRSessionManager cannot be null");
    this.opalRService = opalRService;
    this.opalRuntime = opalRuntime;
    this.opalRSessionManager = opalRSessionManager;
  }

  @Path("/{id}")
  public OpalRSessionResource getSession(@PathParam("id") String id) {
    return new OpalDataShieldSessionResource(opalRService, opalRuntime, opalRSessionManager, opalRSessionManager.getSubjectRSession(id));
  }

  @Path("/current")
  public OpalRSessionResource getCurrentSession() {
    if(opalRSessionManager.hasSubjectCurrentRSession() == false) {
      opalRSessionManager.newSubjectCurrentRSession();
    }
    return new OpalDataShieldSessionResource(opalRService, opalRuntime, opalRSessionManager, opalRSessionManager.getSubjectCurrentRSession());
  }

}
