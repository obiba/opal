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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Dtos {

  private Dtos() {}

  public static ReportTemplate fromDto(ReportTemplateDto reportTemplateDto) {
    ReportTemplate reportTemplate = new ReportTemplate();
    reportTemplate.setName(reportTemplateDto.getName());
    reportTemplate.setDesign(reportTemplateDto.getDesign());
    reportTemplate.setFormat(reportTemplateDto.getFormat());

    Map<String, String> params = Maps.newLinkedHashMap();
    for(ParameterDto param : reportTemplateDto.getParametersList()) {
      params.put(param.getKey(), param.getValue());
    }
    reportTemplate.setParameters(params);

    String schedule = reportTemplateDto.getCron();
    if(schedule != null) {
      reportTemplate.setSchedule(reportTemplateDto.getCron());
    }

    reportTemplate.setEmailNotificationAddresses(Sets.newHashSet(reportTemplateDto.getEmailNotificationList()));

    return reportTemplate;
  }

  public static ReportTemplateDto asDto(ReportTemplate reportTemplate) {
    ReportTemplateDto.Builder dtoBuilder = ReportTemplateDto.newBuilder().setName(reportTemplate.getName())
        .setDesign(reportTemplate.getDesign()).setFormat(reportTemplate.getFormat());
    for(Map.Entry<String, String> param : reportTemplate.getParameters().entrySet()) {
      dtoBuilder.addParameters(ParameterDto.newBuilder().setKey(param.getKey()).setValue(param.getValue()));
    }

    String schedule = reportTemplate.getSchedule();
    if(schedule != null) {
      dtoBuilder.setCron(schedule);
    }

    dtoBuilder.addAllEmailNotification(reportTemplate.getEmailNotificationAddresses());

    return dtoBuilder.build();
  }

  public static Set<ReportTemplateDto> asDto(Iterable<ReportTemplate> reportTemplates) {

    Set<Opal.ReportTemplateDto> reportTemplateDtos = new LinkedHashSet<ReportTemplateDto>();
    for(ReportTemplate reportTemplate : reportTemplates) {
      reportTemplateDtos.add(asDto(reportTemplate));
    }
    return reportTemplateDtos;
  }

}
