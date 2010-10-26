/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.commands;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.reporting.service.ReportException;
import org.obiba.opal.reporting.service.ReportService;
import org.obiba.opal.shell.commands.options.ReportCommandOptions;
import org.springframework.beans.factory.annotation.Autowired;

@CommandUsage(description = "Generate a report based on the specified report template.", syntax = "Syntax: report --name TEMPLATE")
public class ReportCommand extends AbstractOpalRuntimeDependentCommand<ReportCommandOptions> {
  //
  // Constants
  //

  private static final String DATE_FORMAT_PATTERN = "yyyyMMdd_HHmm";

  //
  // Instance Variables
  //

  @Autowired
  private ReportService reportService;

  //
  // AbstractOpalRuntimeDependentCommand Methods
  //

  @Override
  public int execute() {
    // Get the report template.
    String reportTemplateName = getOptions().getName();
    ReportTemplate reportTemplate = this.getOpalRuntime().getOpalConfiguration().getReportTemplate(reportTemplateName);
    if(reportTemplate == null) {
      getShell().printf("Report template '%s' does not exist.\n", reportTemplateName);
      return 1;
    }

    // Render it.
    Date reportDate = new Date();
    try {
      reportService.render(reportTemplate.getFormat(), reportTemplate.getParameters(), getReportDesign(reportTemplate.getDesign()), getReportOutput(reportTemplateName, reportTemplate.getFormat(), reportDate));
    } catch(ReportException ex) {
      getShell().printf("Error rendering report: '%s'\n", ex.getMessage());
      return 2;
    } catch(FileSystemException ex) {
      getShell().printf("Invalid report output destination: '/reports/%s/%s'", reportTemplateName, getReportFileName(reportTemplateName, reportTemplate.getFormat(), reportDate));
      return 3;
    }

    // TODO: Send notification email.

    return 0;
  }

  //
  // Methods
  //

  @Override
  public String toString() {
    return "report -n " + getOptions().getName();
  }

  private String getReportDesign(String reportDesign) throws FileSystemException {
    FileObject reportDesignFile = getFile(reportDesign);

    return getLocalFile(reportDesignFile).getPath();
  }

  private String getReportOutput(String reportTemplateName, String reportFormat, Date reportDate) throws FileSystemException {
    String reportFileName = getReportFileName(reportTemplateName, reportFormat, reportDate);

    FileObject reportDir = getFile("/reports/" + reportTemplateName);
    reportDir.createFolder();
    FileObject reportFile = reportDir.resolveFile(reportFileName);

    return getLocalFile(reportFile).getPath();
  }

  private String getReportFileName(String reportTemplateName, String reportFormat, Date reportDate) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
    String reportDateText = dateFormat.format(reportDate);

    return reportTemplateName + "-" + reportDateText + "." + reportFormat;
  }
}
