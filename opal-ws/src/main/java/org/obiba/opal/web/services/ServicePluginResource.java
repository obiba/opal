package org.obiba.opal.web.services;

import org.obiba.opal.core.runtime.NoSuchServiceException;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.spi.ServicePlugin;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.plugins.PluginResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
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
