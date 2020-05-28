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
