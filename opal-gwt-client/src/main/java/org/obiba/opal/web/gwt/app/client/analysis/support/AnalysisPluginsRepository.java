/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.analysis.support;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.opal.AnalysisPluginPackageDto;
import org.obiba.opal.web.model.client.opal.AnalysisPluginTemplateDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisDto;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;

import java.util.List;

public class AnalysisPluginsRepository {

  private final List<PluginPackageDto> plugins;

  public AnalysisPluginsRepository(List<PluginPackageDto> plugins) {
    this.plugins = plugins;
  }

  public void visitPlugins(PluginTemplateVisitor visitor) {
    for (PluginPackageDto plugin : plugins) {
      AnalysisPluginPackageDto analysisPlugin =
        plugin.getExtension(AnalysisPluginPackageDto.PluginPackageDtoExtensions.analysis).cast();

      List<AnalysisPluginTemplateDto> analysisPluginTemplateDtos = JsArrays.toList(analysisPlugin.getAnalysisTemplatesArray());
      visitor.accept(plugin, analysisPluginTemplateDtos);
    }
  }

  public AnalysisPluginData findAnalysisPluginData(OpalAnalysisDto analysisDto) {
    if (analysisDto == null) return null;

    for (PluginPackageDto plugin : plugins) {
      if (plugin.getName().equals(analysisDto.getPluginName())) {
        AnalysisPluginPackageDto analysisPlugin =
          plugin.getExtension(AnalysisPluginPackageDto.PluginPackageDtoExtensions.analysis).cast();

        for (AnalysisPluginTemplateDto template : JsArrays.toList(analysisPlugin.getAnalysisTemplatesArray())) {
          if (template.getName().equals(analysisDto.getTemplateName())) {
            return new AnalysisPluginData(plugin, template);
          }
        }

      }
    }

    return null;
  }

}
