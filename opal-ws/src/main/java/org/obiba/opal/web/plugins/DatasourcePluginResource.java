package org.obiba.opal.web.plugins;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import jdk.nashorn.internal.objects.annotations.Getter;
import org.obiba.opal.core.cfg.PluginsService;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.spi.datasource.DatasourceService;
import org.obiba.opal.spi.datasource.DatasourceUsage;
import org.obiba.opal.web.model.Plugins;
import org.obiba.plugins.spi.ServicePlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/datasource-plugin/{name}")
public class DatasourcePluginResource {

  @PathParam("name")
  private String name;

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private PluginsService pluginsService;

  @GET
  public Plugins.PluginDto get() {
    return Dtos.asDto(pluginsService.getInstalledPlugin(name));
  }

  @GET
  @Path("form")
  public String getSchemaJsonForUsage(@QueryParam("usage") @DefaultValue("import") String usage) {
    if (opalRuntime.hasServicePlugin(name)) {
      ServicePlugin servicePlugin = opalRuntime.getServicePlugin(name);
      if (servicePlugin instanceof DatasourceService) {
        DatasourceService asDatasourceService = (DatasourceService) servicePlugin;

        return asDatasourceService.getJSONSchemaForm(DatasourceUsage.valueOf(usage.toUpperCase())).toString();
      }
    }

    return "{}";
  }
}
