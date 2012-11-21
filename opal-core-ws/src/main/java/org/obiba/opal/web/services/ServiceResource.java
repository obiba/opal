/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.services;

import java.net.URI;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/service/{name}")
public class ServiceResource {

  @PathParam("name")
  private String name;

  @Autowired
  private Set<Service> services;

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private ServiceConfigurationConverterRegistry configConverter;

  @Autowired
  private OpalConfigurationService configService;

  @Autowired
  private ExtensionConfigurationSupplier configSupplier;

  @Autowired
  public ServiceResource(Set<Service> services, OpalRuntime opalRuntime,
      ServiceConfigurationConverterRegistry configConverter, OpalConfigurationService configService,
      ExtensionConfigurationSupplier configSupplier) {
    this.services = services;
    this.opalRuntime = opalRuntime;
    this.configConverter = configConverter;
    this.configService = configService;
    this.configSupplier = configSupplier;
  }

  @GET
  public Response service() {

    Service service = opalRuntime.getService(name);

    Opal.ServiceStatus status = service.isRunning() ? Opal.ServiceStatus.RUNNING : Opal.ServiceStatus.STOPPED;
    URI link = UriBuilder.fromPath("/").path(ServiceResource.class).build(service.getName());

    Opal.ServiceDto dto = Opal.ServiceDto.newBuilder().setName(service.getName()).setStatus(status)
        .setLink(link.getPath()).build();

    return Response.ok().entity(dto).build();
  }

  @GET
  @Path("/cfg")
  public Response getConfig() {
    Service service = opalRuntime.getService(name);

    Opal.ServiceCfgDto dto = configConverter.get(service.getConfig(), name);

    return Response.ok().entity(dto).build();
  }

  @PUT
  @Path("/cfg")
  public Response saveConfig(Opal.ServiceCfgDto serviceDto) {

    Service service = opalRuntime.getService(name);
    configConverter.put(serviceDto, name);

    return Response.ok().build();

  }
}
