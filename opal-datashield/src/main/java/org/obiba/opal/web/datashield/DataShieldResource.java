/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.datashield;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.cfg.DatashieldConfiguration;
import org.obiba.opal.datashield.cfg.DatashieldConfiguration.Environment;
import org.obiba.opal.datashield.cfg.DatashieldConfigurationSupplier;
import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.datashield.support.DataShieldMethodConverterRegistry;
import org.obiba.opal.web.r.OpalRSessionResource;
import org.obiba.opal.web.r.OpalRSessionsResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/datashield")
public class DataShieldResource {

  private final DatashieldConfigurationSupplier configurationSupplier;

  private final OpalRSessionManager opalRSessionManager;

  private final DataShieldMethodConverterRegistry methodConverterRegistry;

  @Autowired
  public DataShieldResource(DatashieldConfigurationSupplier configurationSupplier, OpalRSessionManager opalRSessionManager, DataShieldMethodConverterRegistry methodConverterRegistry) {
    if(configurationSupplier == null) throw new IllegalArgumentException("configurationSupplier cannot be null");
    if(opalRSessionManager == null) throw new IllegalArgumentException("opalRSessionManager cannot be null");
    if(methodConverterRegistry == null) throw new IllegalArgumentException("methodConverterRegistry cannot be null");
    this.configurationSupplier = configurationSupplier;
    this.opalRSessionManager = opalRSessionManager;
    this.methodConverterRegistry = methodConverterRegistry;
  }

  @Path("/sessions")
  public OpalRSessionsResource getSessions() {
    return new OpalRSessionsResource(opalRSessionManager) {
      @Override
      protected void onNewSession(OpalRSession rSession) {
        onNewDataShieldSession(rSession);
      }
    };
  }

  @Path("/session/{id}")
  public OpalRSessionResource getSession(@PathParam("id") String id) {
    return new OpalDataShieldSessionResource(configurationSupplier, opalRSessionManager, opalRSessionManager.getSubjectRSession(id));
  }

  @Path("/session/current")
  public OpalRSessionResource getCurrentSession() {
    if(opalRSessionManager.hasSubjectCurrentRSession() == false) {
      OpalRSession session = opalRSessionManager.newSubjectCurrentRSession();
      onNewDataShieldSession(session);
    }
    return new OpalDataShieldSessionResource(configurationSupplier, opalRSessionManager, opalRSessionManager.getSubjectCurrentRSession());
  }

  @Path("/env/{name}")
  public DataShieldEnvironmentResource getEnvironment(@PathParam("name") String env) {
    return new DataShieldEnvironmentResource(Environment.valueOf(env.toUpperCase()), configurationSupplier, methodConverterRegistry);
  }

  @GET
  @Path("/cfg")
  public Response getConfig() {
    // TODO: implement GET and PUT for altering the restriction level through the UI
    return Response.ok().build();
  }

  protected void onNewDataShieldSession(OpalRSession session) {
    // TODO: assign methods to specific environments within R.
    DatashieldConfiguration config = configurationSupplier.get();
    for(Environment environment : Environment.values()) {
      config.getEnvironment(environment).prepare(session);
    }
    DataShieldLog.userLog("created a datashield session {}", session.getId());
  }

}
