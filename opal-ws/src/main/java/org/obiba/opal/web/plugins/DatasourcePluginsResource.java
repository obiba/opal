package org.obiba.opal.web.plugins;

import org.obiba.opal.core.cfg.PluginsService;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.spi.datasource.DatasourceService;
import org.obiba.opal.spi.datasource.DatasourceUsage;
import org.obiba.opal.web.model.Plugins;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.stream.Collectors;

@Component
@Path("/datasource-plugins")
public class DatasourcePluginsResource {
  
  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private PluginsService pluginsService;

  @GET
  public Plugins.PluginPackagesDto list(@QueryParam("usage") @DefaultValue("import") String usage) {
    DatasourceUsage dsUsage = DatasourceUsage.valueOf(usage.toUpperCase());
    return Dtos.asDto(pluginsService.getUpdateSite(), pluginsService.getLastUpdate(), pluginsService.restartRequired(),
        pluginsService.getInstalledPlugins().stream()
            .filter(p -> DatasourceService.SERVICE_TYPE.equals(p.getType()))
            .filter(p -> ((DatasourceService) opalRuntime.getServicePlugin(p.getName())).getUsages().contains(dsUsage))
            .collect(Collectors.toList()));
  }

}
