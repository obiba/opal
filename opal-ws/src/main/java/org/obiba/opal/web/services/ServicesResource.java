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

import java.net.URI;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.UriBuilder;

import org.obiba.opal.core.runtime.App;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.runtime.Service;
import org.obiba.plugins.spi.ServicePlugin;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.plugins.PluginResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Component
@Transactional
@Scope("request")
@Path("/services")
public class ServicesResource {

  @Autowired
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private Set<Service> services;

  @Autowired
  private OpalRuntime opalRuntime;

  @GET
  public List<Opal.ServiceDto> services() {
    List<Opal.ServiceDto> serviceDtos = Lists.newArrayList();

    for(Service service : services) {
      Opal.ServiceStatus status = service.isRunning() ? Opal.ServiceStatus.RUNNING : Opal.ServiceStatus.STOPPED;
      URI link = UriBuilder.fromPath("/").path(ServiceResource.class).build(service.getName());
      Opal.ServiceDto dto = Opal.ServiceDto.newBuilder().setName(service.getName()).setStatus(status)
          .setLink(link.getPath()).build();
      serviceDtos.add(dto);
    }

    for (ServicePlugin service : opalRuntime.getServicePlugins()) {
      Opal.ServiceStatus status = service.isRunning() ? Opal.ServiceStatus.RUNNING : Opal.ServiceStatus.STOPPED;
      URI link = UriBuilder.fromPath("/").path(PluginResource.class).segment("service").build(service.getName());
      Opal.ServiceDto dto = Opal.ServiceDto.newBuilder().setName(service.getName()).setStatus(status)
          .setLink(link.getPath()).build();
      serviceDtos.add(dto);
    }

    for (App app : opalRuntime.getApps()) {
      Opal.ServiceStatus status = Opal.ServiceStatus.RUNNING;
      URI link = UriBuilder.fromPath("/").path(PluginResource.class).segment("service").build(app.getName());
      Opal.ServiceDto dto = Opal.ServiceDto.newBuilder().setName(app.getName()).setStatus(status)
              .setLink(link.getPath()).build();
      serviceDtos.add(dto);
    }


    return serviceDtos;
  }

}
