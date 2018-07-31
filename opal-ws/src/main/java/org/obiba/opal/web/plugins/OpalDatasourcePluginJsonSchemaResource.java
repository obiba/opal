package org.obiba.opal.web.plugins;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.spi.datasource.DatasourceService;
import org.obiba.opal.spi.datasource.DatasourceUsage;
import org.obiba.plugins.spi.ServicePlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/dsplugin/{name}")
public class OpalDatasourcePluginJsonSchemaResource {

  @Autowired
  private OpalRuntime opalRuntime;

  @GET
  public String getSchemaJsonForUsage(@PathParam("name") String name, @QueryParam("usage") @DefaultValue("import") String usage) {
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
