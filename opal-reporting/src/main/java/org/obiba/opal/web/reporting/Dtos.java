/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.reporting;

import com.google.common.base.Strings;
import org.obiba.opal.core.domain.ReportTemplate;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.ParameterDto;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Dtos {

  private Dtos() {
  }

  public static ReportTemplate fromDto(ReportTemplateDto dto) {
    return fromDto(dto, null);
  }

  public static ReportTemplate fromDto(ReportTemplateDto dto, ReportTemplate original) {
    ReportTemplate.Builder builder = ReportTemplate.Builder.create() //
        .nameAndProject(dto.getName(), dto.getProject()) //
        .design(dto.getDesign()) //
        .format(dto.getFormat());
    if (dto.hasCron()) builder.schedule(dto.getCron());
    for (ParameterDto param : dto.getParametersList()) {
      String value = param.getValue();
      // if empty password/token then means not modified
      if (original != null && Strings.isNullOrEmpty(value)
          && ("opal.password".equals(param.getKey()) || "opal.token".equals(param.getKey()))
          && original.getParameters().containsKey(param.getKey()))
        value = original.getParameters().get(param.getKey());
      builder.parameter(param.getKey(), value);
    }
    builder.emailNotificationAddresses(dto.getEmailNotificationList());
    builder.failureEmailNotificationAddresses(dto.getFailureEmailNotificationList());
    return builder.build();
  }

  public static ReportTemplateDto asDto(ReportTemplate reportTemplate) {
    ReportTemplateDto.Builder builder = ReportTemplateDto.newBuilder() //
        .setName(reportTemplate.getName()) //
        .setProject(reportTemplate.getProject()) //
        .setDesign(reportTemplate.getDesign()) //
        .setFormat(reportTemplate.getFormat());
    for (Map.Entry<String, String> param : reportTemplate.getParameters().entrySet()) {
      String value = param.getValue();
      if ("opal.password".equals(param.getKey()) || "opal.token".equals(param.getKey())) {
        value = "";
      }
      builder.addParameters(ParameterDto.newBuilder().setKey(param.getKey()).setValue(value));
    }
    if (reportTemplate.hasSchedule()) builder.setCron(reportTemplate.getSchedule());
    builder.addAllEmailNotification(reportTemplate.getEmailNotificationAddresses());
    builder.addAllFailureEmailNotification(reportTemplate.getFailureEmailNotificationAddresses());
    return builder.build();
  }

  public static Set<ReportTemplateDto> asDto(Iterable<ReportTemplate> reportTemplates) {
    Set<Opal.ReportTemplateDto> reportTemplateDtos = new LinkedHashSet<>();
    for (ReportTemplate reportTemplate : reportTemplates) {
      reportTemplateDtos.add(asDto(reportTemplate));
    }
    return reportTemplateDtos;
  }

}
