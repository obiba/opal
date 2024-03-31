/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.datashield;

import org.obiba.datashield.core.DSMethodType;
import org.obiba.opal.core.DeprecatedOperationException;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.r.RSessionsResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Component
@Transactional
@Path("/datashield")
public class DataShieldResource {

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @Autowired
  private ApplicationContext applicationContext;

  @Path("/sessions")
  public RSessionsResource getSessions() {
    RSessionsResource resource = applicationContext
        .getBean("datashieldSessionsResource", RSessionsResource.class);
    return resource;
  }

  @Path("/session/{id}")
  public DataShieldSessionResource getSession(@PathParam("id") String id) {
    DataShieldSessionResource resource = applicationContext
        .getBean("dataShieldSessionResource", DataShieldSessionResource.class);
    resource.setRServerSession(opalRSessionManager.getSubjectRSession(id));
    return resource;
  }

  @Path("/session/current")
  public DataShieldSessionResource getCurrentSession() {
    throw new DeprecatedOperationException("Unsupported operation: please upgrade your opal R package.");
  }

  @Path("/env/{name}")
  public DataShieldEnvironmentResource getEnvironment(@PathParam("name") String env) {
    DataShieldEnvironmentResource resource = applicationContext.getBean(DataShieldEnvironmentResource.class);
    resource.setMethodType(DSMethodType.valueOf(env.toUpperCase()));
    return resource;
  }

}
