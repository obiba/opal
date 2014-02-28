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

import org.obiba.magma.security.Authorizer;
import org.obiba.magma.security.shiro.ShiroAuthorizer;
import org.obiba.opal.core.domain.ReportTemplate;
import org.obiba.opal.core.service.ReportTemplateService;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;
import org.obiba.opal.web.reporting.Dtos;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
public abstract class AbstractReportTemplateResource {

  protected static final String REPORT_SCHEDULING_GROUP = "reports";

  protected CommandSchedulerService commandSchedulerService;

  protected ReportTemplateService reportTemplateService;

  private final Authorizer authorizer = new ShiroAuthorizer();

  protected boolean reportTemplateExists(String name, String project) {
    return reportTemplateService.hasReportTemplate(name, project);
  }

  protected void save(ReportTemplateDto dto) {
    reportTemplateService.save(Dtos.fromDto(dto));
  }

  protected void updateSchedule(ReportTemplateDto reportTemplateDto) {
    String name = reportTemplateDto.getName();
    commandSchedulerService.unscheduleCommand(name, REPORT_SCHEDULING_GROUP);
    if(reportTemplateDto.hasCron()) {
      commandSchedulerService.scheduleCommand(name, REPORT_SCHEDULING_GROUP, reportTemplateDto.getCron());
    }
  }

  protected Authorizer getAuthorizer() {
    return authorizer;
  }

  protected boolean authzReadReportTemplate(ReportTemplate template) {
    return authorizer.isPermitted("rest:/report-template/" + template.getName() + ":GET");
  }

  @Autowired
  public void setCommandSchedulerService(CommandSchedulerService commandSchedulerService) {
    this.commandSchedulerService = commandSchedulerService;
  }

  @Autowired
  public void setReportTemplateService(ReportTemplateService reportTemplateService) {
    this.reportTemplateService = reportTemplateService;
  }
}
