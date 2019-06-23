/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.analysis.support;

import com.google.gwt.core.client.JsArray;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.opal.AnalyseCommandOptionsDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisDto;

public class AnalyseCommandOptionsFactory {

  public static AnalyseCommandOptionsDto create(OpalAnalysisDto analysisDto) {
    AnalyseCommandOptionsDto command = AnalyseCommandOptionsDto.create();
    command.setProject(analysisDto.getDatasource());

    AnalyseCommandOptionsDto.AnalyseDto analyse = AnalyseCommandOptionsDto.AnalyseDto.create();
    analyse.setName(analysisDto.getName());
    analyse.setPlugin(analysisDto.getPluginName());
    analyse.setTemplate(analysisDto.getTemplateName());
    analyse.setTable(analysisDto.getTable());
    analyse.setParams(analysisDto.getParameters());

    if (analysisDto.getVariablesCount() > 0)
      analyse.setVariables(analysisDto.getVariablesArray().join());

    JsArray<AnalyseCommandOptionsDto.AnalyseDto> analyses = JsArrays.create();
    analyses.push(analyse);
    command.setAnalysesArray(analyses);

    return command;
  }
}
