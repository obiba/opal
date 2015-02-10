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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.DeprecatedOperationException;
import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier.ExtensionConfigModificationTask;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.cfg.DatashieldConfiguration;
import org.obiba.opal.datashield.cfg.DatashieldConfiguration.Environment;
import org.obiba.opal.datashield.cfg.DatashieldConfigurationSupplier;
import org.obiba.opal.r.RScriptROperation;
import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.datashield.support.DataShieldROptionsScriptBuilder;
import org.obiba.opal.web.model.DataShield.DataShieldConfigDto;
import org.obiba.opal.web.r.OpalRSessionsResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Path("/datashield")
public class DataShieldResource {

  @Autowired
  private DatashieldConfigurationSupplier configurationSupplier;

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @Autowired
  private OpalRSessionsResource opalRSessionsResource;

  @Autowired
  private ApplicationContext applicationContext;

  @Path("/sessions")
  public OpalRSessionsResource getSessions() {
    return opalRSessionsResource;
  }

  @Path("/session/{id}")
  public OpalDataShieldSessionResource getSession(@PathParam("id") String id) {
    OpalDataShieldSessionResource resource = applicationContext
        .getBean("opalDataShieldSessionResource", OpalDataShieldSessionResource.class);
    resource.setOpalRSession(opalRSessionManager.getSubjectRSession(id));
    return resource;
  }

  @Path("/session/current")
  public OpalDataShieldSessionResource getCurrentSession() {
    throw new DeprecatedOperationException("Unsupported operation: please upgrade your opal R package.");
  }

  @Path("/env/{name}")
  public DataShieldEnvironmentResource getEnvironment(@PathParam("name") String env) {
    DataShieldEnvironmentResource resource = applicationContext.getBean(DataShieldEnvironmentResource.class);
    resource.setEnvironment(Environment.valueOf(env.toUpperCase()));
    return resource;
  }

  @GET
  @Path("/cfg")
  public Response getConfig() {
    DataShieldConfigDto.Level level = DataShieldConfigDto.Level.valueOf(configurationSupplier.get().getLevel().name());
    return Response.ok(DataShieldConfigDto.newBuilder().setLevel(level).build()).build();
  }

  @PUT
  @Path("/cfg")
  public Response setConfig(DataShieldConfigDto config) {
    final DatashieldConfiguration.Level level = DatashieldConfiguration.Level.valueOf(config.getLevel().name());
    configurationSupplier.modify(new ExtensionConfigModificationTask<DatashieldConfiguration>() {

      @Override
      public void doWithConfig(DatashieldConfiguration config) {
        config.setLevel(level);
      }
    });
    return getConfig();
  }

  protected void onNewDataShieldSession(OpalRSession session) {
    DatashieldConfiguration config = configurationSupplier.get();
    if (config.hasOptions()) {
      session.execute(
          new RScriptROperation(DataShieldROptionsScriptBuilder.newBuilder().setROptions(config.getOptions()).build()));
    }
    DataShieldLog.userLog("created a datashield session {}", session.getId());
  }

}
