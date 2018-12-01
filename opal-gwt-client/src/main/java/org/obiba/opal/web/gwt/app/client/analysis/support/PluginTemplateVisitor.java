package org.obiba.opal.web.gwt.app.client.analysis.support;

import org.obiba.opal.web.model.client.opal.AnalysisPluginTemplateDto;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;

import java.util.List;

public interface PluginTemplateVisitor {
  void accept(PluginPackageDto plugin, List<AnalysisPluginTemplateDto> templates);
}
