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

import org.obiba.opal.core.DeprecatedOperationException;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Component
@Transactional
@Scope("request")
@Path("/r/session")
public class OpalRSessionParentResource {

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @Autowired
  private ApplicationContext applicationContext;

  @Path("/{id}")
  public OpalRSessionResource getOpalRSessionResource(@PathParam("id") String id) {
    OpalRSessionResource resource = applicationContext.getBean("opalRSessionResource", OpalRSessionResource.class);
    resource.setOpalRSession(opalRSessionManager.getSubjectRSession(id));
    return resource;
  }

  @Path("/current")
  public OpalRSessionResource getCurrentOpalRSessionResource() {
    throw new DeprecatedOperationException("Unsupported operation: please upgrade your opal R package.");
  }

}
