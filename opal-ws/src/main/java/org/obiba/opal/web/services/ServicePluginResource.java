package org.obiba.opal.web.services;

import org.obiba.opal.core.runtime.NoSuchServiceException;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ServicePluginResource {

  public enum PLUGIN_STATUS { RUNNING, STOPPED }

  private String service;

  @Autowired
  private OpalRuntime opalRuntime;

  @GET
  public Response get() {
    return Response.ok().entity(opalRuntime.getServicePlugin(service).isRunning() ? PLUGIN_STATUS.RUNNING : PLUGIN_STATUS.STOPPED).build();
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
