package org.obiba.opal.web.gwt.app.client.analysis.service;

import com.google.inject.Singleton;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.opal.AnalysisPluginPackageDto;
import org.obiba.opal.web.model.client.opal.AnalysisPluginTemplateDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisDto;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class PluginService {

  private List<PluginPackageDto> plugins = new ArrayList<PluginPackageDto>();

  public void travers(PluginVisitor visitor) {
    for (PluginPackageDto plugin : plugins) {
      AnalysisPluginPackageDto analysisPlugin =
        plugin.getExtension(AnalysisPluginPackageDto.PluginPackageDtoExtensions.analysis).cast();

      List<AnalysisPluginTemplateDto> analysisPluginTemplateDtos = JsArrays.toList(analysisPlugin.getAnalysisTemplatesArray());
      visitor.accept(plugin, analysisPluginTemplateDtos);
    }
  }

  public OpalAnalysisPluginData getAnalysisPluginData(OpalAnalysisDto analysisDto) {
    if (analysisDto == null) return null;

    for (PluginPackageDto plugin : plugins) {
      if (plugin.getName().equals(analysisDto.getPluginName())) {
        AnalysisPluginPackageDto analysisPlugin =
          plugin.getExtension(AnalysisPluginPackageDto.PluginPackageDtoExtensions.analysis).cast();

        for (AnalysisPluginTemplateDto template : JsArrays.toList(analysisPlugin.getAnalysisTemplatesArray())) {
          if (template.getName().equals(analysisDto.getTemplateName())) {
            return new OpalAnalysisPluginData(plugin, template);
          }
        }

      }
    }

    return null;
  }

  public void setPlugins(List<PluginPackageDto> value) {
    if (value != null) plugins = value;
  }
}
