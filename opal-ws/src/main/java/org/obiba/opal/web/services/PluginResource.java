/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.services;

import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.web.model.Plugins;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Component
@Scope("request")
@Path("/plugin/{name}")
public class PluginResource {

  @PathParam("name")
  private String name;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private OpalRuntime opalRuntime;

  @GET
  public Plugins.PluginDto get() {
    return Dtos.asDto(opalRuntime.getPlugin(name));
  }

  @Path("/service")
  public ServicePluginResource service() {
    ServicePluginResource resource = applicationContext.getBean(ServicePluginResource.class);
    resource.setService(name);
    return resource;
  }

  @GET
  @Path("/cfg")
  public Plugins.PluginCfgDto getConfig() {
    return get().getConfig();
  }

  @PUT
  @Path("/cfg")
  public Response saveConfig(Plugins.PluginCfgDto configDto) {
    // TODO: write plugin properties
    return Response.status(Response.Status.NOT_IMPLEMENTED).build();
  }

}
