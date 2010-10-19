/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.reporting;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.ParameterDto;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;

public class Dtos {

  public static ReportTemplateDto asDto(ReportTemplate reportTemplate) {
    ReportTemplateDto.Builder dtoBuilder = ReportTemplateDto.newBuilder().setName(reportTemplate.getName()).setDesign(reportTemplate.getDesign()).setFormat(reportTemplate.getFormat());
    for(Map.Entry<String, String> param : reportTemplate.getParameters().entrySet()) {
      dtoBuilder.addParameters(ParameterDto.newBuilder().setKey(param.getKey()).setValue(param.getValue()));
    }
    String schedule = reportTemplate.getSchedule();
    if(schedule != null) {
      dtoBuilder.setCron(schedule);
    }
    return dtoBuilder.build();
  }

  public static Set<ReportTemplateDto> asDto(Set<ReportTemplate> reportTemplates) {

    Set<Opal.ReportTemplateDto> reportTemplateDtos = new LinkedHashSet<ReportTemplateDto>();
    for(ReportTemplate reportTemplate : reportTemplates) {
      reportTemplateDtos.add(asDto(reportTemplate));
    }
    return reportTemplateDtos;
  }

}
