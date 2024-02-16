/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.plugins;

import org.obiba.opal.core.cfg.PluginsService;
import org.obiba.opal.web.model.Plugins;
import org.obiba.opal.web.services.ServicePluginResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Component
@Scope("request")
@Path("/plugin/{name}")
public class PluginResource {

  @PathParam("name")
  private String name;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private PluginsService pluginsService;

  @GET
  public Plugins.PluginDto get() {
    return Dtos.asDto(pluginsService.getInstalledPlugin(name));
  }

  @DELETE
  public Response uninstall() {
    pluginsService.prepareUninstallPlugin(name);
    return Response.noContent().build();
  }

  @PUT
  public Response cancelUninstallation() {
    pluginsService.cancelUninstallPlugin(name);
    return Response.noContent().build();
  }

  @Path("/service")
  public ServicePluginResource service() {
    ServicePluginResource resource = applicationContext.getBean(ServicePluginResource.class);
    resource.setService(name);
    return resource;
  }

  @PUT
  @Path("/cfg")
  @Consumes("text/plain")
  public Response saveConfig(String properties) {
    pluginsService.setInstalledPluginSiteProperties(name, properties);
    return Response.ok().build();
  }

}
