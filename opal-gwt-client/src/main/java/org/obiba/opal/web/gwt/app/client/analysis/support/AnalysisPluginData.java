package org.obiba.opal.web.gwt.app.client.analysis.support;

import org.obiba.opal.web.model.client.opal.AnalysisPluginTemplateDto;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;

public class AnalysisPluginData {
  private PluginPackageDto pluginDto;
  private AnalysisPluginTemplateDto templateDto;

  AnalysisPluginData(PluginPackageDto pluginDto, AnalysisPluginTemplateDto templateDto) {
    this.pluginDto = pluginDto;
    this.templateDto = templateDto;
  }

  public PluginPackageDto getPluginDto() {
    if (pluginDto == null) throw new IllegalArgumentException("pluginDto is null.");
    return pluginDto;
  }

  public AnalysisPluginTemplateDto getTemplateDto() {
    if (templateDto == null) throw new IllegalArgumentException("templateDto is null.");
    return templateDto;
  }

  public boolean hasPluginDto() {
    return pluginDto != null;
  }

  public boolean hasTemplateDto() {
    return templateDto != null;
  }

}
