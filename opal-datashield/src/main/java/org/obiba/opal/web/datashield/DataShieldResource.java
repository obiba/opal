/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.datashield;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.datashield.core.DSMethodType;
import org.obiba.opal.core.DeprecatedOperationException;
import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier.ExtensionConfigModificationTask;
import org.obiba.opal.datashield.cfg.DatashieldConfiguration;
import org.obiba.opal.datashield.cfg.DatashieldConfigurationSupplier;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.model.DataShield.DataShieldConfigDto;
import org.obiba.opal.web.r.RSessionsResource;
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
    resource.setOpalRSession(opalRSessionManager.getSubjectRSession(id));
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



}
