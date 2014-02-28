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

import org.obiba.opal.core.domain.ReportTemplate;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.ParameterDto;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;

public class Dtos {

  private Dtos() {}

  public static ReportTemplate fromDto(ReportTemplateDto dto) {
    ReportTemplate.Builder builder = ReportTemplate.Builder.create() //
        .nameAndProject(dto.getName(), dto.getProject()) //
        .design(dto.getDesign()) //
        .format(dto.getFormat());
    if(dto.hasCron()) builder.schedule(dto.getCron());
    for(ParameterDto param : dto.getParametersList()) {
      builder.parameter(param.getKey(), param.getValue());
    }
    builder.emailNotificationAddresses(dto.getEmailNotificationList());
    return builder.build();
  }

  public static ReportTemplateDto asDto(ReportTemplate reportTemplate) {
    ReportTemplateDto.Builder builder = ReportTemplateDto.newBuilder() //
        .setName(reportTemplate.getName()) //
        .setProject(reportTemplate.getProject()) //
        .setDesign(reportTemplate.getDesign()) //
        .setFormat(reportTemplate.getFormat());
    for(Map.Entry<String, String> param : reportTemplate.getParameters().entrySet()) {
      builder.addParameters(ParameterDto.newBuilder().setKey(param.getKey()).setValue(param.getValue()));
    }
    if(reportTemplate.hasSchedule()) builder.setCron(reportTemplate.getSchedule());
    builder.addAllEmailNotification(reportTemplate.getEmailNotificationAddresses());
    return builder.build();
  }

  public static Set<ReportTemplateDto> asDto(Iterable<ReportTemplate> reportTemplates) {
    Set<Opal.ReportTemplateDto> reportTemplateDtos = new LinkedHashSet<>();
    for(ReportTemplate reportTemplate : reportTemplates) {
      reportTemplateDtos.add(asDto(reportTemplate));
    }
    return reportTemplateDtos;
  }

}
