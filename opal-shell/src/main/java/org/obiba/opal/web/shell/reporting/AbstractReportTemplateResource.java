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

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.security.Authorizer;
import org.obiba.magma.security.MagmaSecurityExtension;
import org.obiba.magma.security.shiro.ShiroAuthorizer;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.cfg.OpalConfigurationService.ConfigModificationTask;
import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;
import org.obiba.opal.web.reporting.Dtos;

/**
 *
 */
public abstract class AbstractReportTemplateResource {

  protected static final String REPORT_SCHEDULING_GROUP = "reports";

  protected abstract OpalConfigurationService getOpalConfigurationService();

  protected abstract CommandSchedulerService getCommandSchedulerService();

  private final Authorizer authorizer = new ShiroAuthorizer();

  protected boolean reportTemplateExists(String name) {
    return name != null && getOpalConfigurationService().getOpalConfiguration().hasReportTemplate(name);
  }

  protected void updateOpalConfiguration(final ReportTemplateDto dto) {
    getOpalConfigurationService().modifyConfiguration(new ConfigModificationTask() {

      @Override
      public void doWithConfig(OpalConfiguration config) {
        OpalConfiguration opalConfig = getOpalConfigurationService().getOpalConfiguration();

        if(opalConfig.hasReportTemplate(dto.getName())) opalConfig.removeReportTemplate(dto.getName());

        opalConfig.addReportTemplate(Dtos.fromDto(dto));
      }
    });

  }

  protected void updateSchedule(ReportTemplateDto reportTemplateDto) {
    String name = reportTemplateDto.getName();
    getCommandSchedulerService().unscheduleCommand(name, REPORT_SCHEDULING_GROUP);
    if(reportTemplateDto.hasCron()) {
      getCommandSchedulerService().scheduleCommand(name, REPORT_SCHEDULING_GROUP, reportTemplateDto.getCron());
    }
  }

  protected Authorizer getAuthorizer() {
    return authorizer;
  }

  protected boolean authzReadReportTemplate(ReportTemplate template) {
    return authorizer.isPermitted("magma:/report-template/" + template.getName() + ":GET");
  }

}
