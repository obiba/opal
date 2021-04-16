/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
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

import java.util.List;

public interface PluginTemplateVisitor {
  void accept(PluginPackageDto plugin, List<AnalysisPluginTemplateDto> templates);
}
