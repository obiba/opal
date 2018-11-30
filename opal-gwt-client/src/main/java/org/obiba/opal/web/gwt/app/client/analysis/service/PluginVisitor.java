package org.obiba.opal.web.gwt.app.client.analysis.service;

import org.obiba.opal.web.model.client.opal.AnalysisPluginTemplateDto;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;

import java.util.List;

public interface PluginVisitor {
  void accept(PluginPackageDto plugin, List<AnalysisPluginTemplateDto> templates);
}
