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

import org.obiba.opal.core.DeprecatedOperationException;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

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
  public RSessionResource getOpalRSessionResource(@PathParam("id") String id) {
    RSessionResource resource = applicationContext.getBean("opalRSessionResource", RSessionResource.class);
    resource.setRServerSession(opalRSessionManager.getSubjectRSession(id));
    return resource;
  }

  @Path("/current")
  public RSessionResource getCurrentOpalRSessionResource() {
    throw new DeprecatedOperationException("Unsupported operation: please upgrade your opal R package.");
  }

}
