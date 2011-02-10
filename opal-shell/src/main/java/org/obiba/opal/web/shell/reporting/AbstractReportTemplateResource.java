/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.shell.reporting;

import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;
import org.obiba.opal.web.reporting.Dtos;

/**
 *
 */
public abstract class AbstractReportTemplateResource {
  protected static final String REPORT_SCHEDULING_GROUP = "reports";

  protected abstract OpalRuntime getOpalRuntime();

  protected abstract CommandSchedulerService getCommandSchedulerService();

  protected boolean reportTemplateExists(String name) {
    return name != null && getOpalRuntime().getOpalConfiguration().hasReportTemplate(name);
  }

  protected void updateOpalConfiguration(ReportTemplateDto dto) {
    OpalConfiguration opalConfig = getOpalRuntime().getOpalConfiguration();

    ReportTemplate reportTemplate = opalConfig.getReportTemplate(dto.getName());
    if(reportTemplate != null) {
      opalConfig.removeReportTemplate(dto.getName());
    }

    reportTemplate = Dtos.fromDto(dto);
    opalConfig.addReportTemplate(reportTemplate);
    getOpalRuntime().writeOpalConfiguration();
  }

  protected void updateSchedule(ReportTemplateDto reportTemplateDto) {
    String name = reportTemplateDto.getName();
    getCommandSchedulerService().unscheduleCommand(name, REPORT_SCHEDULING_GROUP);
    if(reportTemplateDto.hasCron()) {
      getCommandSchedulerService().scheduleCommand(name, REPORT_SCHEDULING_GROUP, reportTemplateDto.getCron());
    }
  }

}
