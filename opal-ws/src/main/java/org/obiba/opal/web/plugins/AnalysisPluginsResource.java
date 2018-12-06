package org.obiba.opal.web.plugins;

import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.obiba.opal.core.cfg.PluginsService;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.spi.analysis.AnalysisService;
import org.obiba.opal.spi.r.analysis.RAnalysisService;
import org.obiba.opal.web.model.Plugins;
import org.obiba.opal.web.model.Plugins.AnalysisPluginPackageDto;
import org.obiba.opal.web.model.Plugins.PluginPackageDto;
import org.obiba.opal.web.model.Plugins.PluginPackageDto.Builder;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/analysis-plugins")
public class AnalysisPluginsResource {

  private final OpalRuntime opalRuntime;

  private final PluginsService pluginsService;

  @Autowired
  public AnalysisPluginsResource(OpalRuntime opalRuntime,
      PluginsService pluginsService) {
    this.opalRuntime = opalRuntime;
    this.pluginsService = pluginsService;
  }

  @GET
  @NoAuthorization
  public Plugins.PluginPackagesDto list() {
    List<PluginPackageDto> packageDtos = pluginsService.getInstalledPlugins().stream()
        .filter(pluginPackage -> AnalysisService.SERVICE_TYPE.equals(pluginPackage.getType()) || RAnalysisService.SERVICE_TYPE.equals(pluginPackage.getType()))
        .map(pluginPackage -> {
          Builder builder = Dtos.asDto(pluginPackage, null).toBuilder();
          return builder.setExtension(AnalysisPluginPackageDto.analysis,
              Dtos.asDto((AnalysisService) opalRuntime.getServicePlugin(pluginPackage.getName()))
                  .build()).build();
        })
        .collect(Collectors.toList());

    return Dtos.asDto(pluginsService.getUpdateSite(), pluginsService.getLastUpdate(), pluginsService.restartRequired())
        .addAllPackages(packageDtos).build();
  }
}
