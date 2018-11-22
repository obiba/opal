package org.obiba.opal.web.plugins;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.spi.analysis.AnalysisService;
import org.obiba.plugins.spi.ServicePlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/analysis-plugin")
public class AnalysisPluginResource {

  private final OpalRuntime opalRuntime;

  @Autowired
  public AnalysisPluginResource(OpalRuntime opalRuntime) {
    this.opalRuntime = opalRuntime;
  }

  @GET
  @Path("/{plg}")
  public Response get(@PathParam("plg") String pluginName) {
    if (opalRuntime.hasServicePlugin(pluginName)) {
      ServicePlugin servicePlugin = opalRuntime.getServicePlugin(pluginName);

      if (servicePlugin instanceof AnalysisService) {
        return Response.ok(Dtos.asDto((AnalysisService) servicePlugin).build()).build();
      }

    }

    return Response.status(Status.NOT_FOUND).build();
  }
}
