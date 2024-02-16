/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.services;

import org.obiba.opal.core.runtime.NoSuchServiceException;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.plugins.spi.ServicePlugin;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.plugins.PluginResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ServicePluginResource {

  private String service;

  @Autowired
  private OpalRuntime opalRuntime;

  @GET
  public Response get() {
    ServicePlugin plugin = opalRuntime.getServicePlugin(service);
    Opal.ServiceStatus status = plugin.isRunning() ? Opal.ServiceStatus.RUNNING : Opal.ServiceStatus.STOPPED;
    URI link = UriBuilder.fromPath("/").path(PluginResource.class).segment("service").build(plugin.getName());
    Opal.ServiceDto dto = Opal.ServiceDto.newBuilder().setName(plugin.getName()).setStatus(status)
        .setLink(link.getPath()).build();
    return Response.ok().entity(dto).build();
  }

  @PUT
  public Response start() {
    opalRuntime.getServicePlugin(service).start();
    return Response.ok().build();
  }

  @DELETE
  public Response stop() {
    try {
      opalRuntime.getServicePlugin(service).stop();
    } catch (NoSuchServiceException e) {
      // ignore
    }

    return Response.noContent().build();
  }

  public void setService(String service) {
    this.service = service;
  }
}
