/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.shell.reporting;

import org.obiba.opal.core.domain.ReportTemplate;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.options.ReportCommandOptions;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ReportTemplateScheduler {

  private static final String REPORT_SCHEDULING_GROUP = "reports";

  private CommandSchedulerService commandSchedulerService;

  private CommandRegistry commandRegistry;

  @Autowired
  public void setCommandSchedulerService(CommandSchedulerService commandSchedulerService) {
    this.commandSchedulerService = commandSchedulerService;
  }

  @Autowired
  @Qualifier("web")
  public void setCommandRegistry(CommandRegistry commandRegistry) {
    this.commandRegistry = commandRegistry;
  }

  void updateSchedule(Opal.ReportTemplateDto dto) {
    String jobName = dto.getProject() + "." + dto.getName();
    commandSchedulerService.unscheduleCommand(jobName, REPORT_SCHEDULING_GROUP);
    if(dto.hasCron()) {
      commandSchedulerService.scheduleCommand(jobName, REPORT_SCHEDULING_GROUP, dto.getCron());
    }
  }

  void deleteSchedule(ReportTemplate reportTemplate) {
    String jobName = reportTemplate.getProject() + "." + reportTemplate.getName();
    commandSchedulerService.deleteCommand(jobName, REPORT_SCHEDULING_GROUP);
  }

  void scheduleCommand(final Opal.ReportTemplateDto dto) {
    ReportCommandOptions reportOptions = new ReportCommandOptions() {

      @Override
      public boolean isHelp() {
        return false;
      }

      @Override
      public String getName() {
        return dto.getName();
      }

      @Override
      public String getProject() {
        return dto.getProject();
      }
    };
    Command<ReportCommandOptions> reportCommand = commandRegistry.newCommand("report");
    reportCommand.setOptions(reportOptions);
    String jobName = dto.getProject() + "." + dto.getName();
    commandSchedulerService.addCommand(jobName, REPORT_SCHEDULING_GROUP, reportCommand);
  }

}
