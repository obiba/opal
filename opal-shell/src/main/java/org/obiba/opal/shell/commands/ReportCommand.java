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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.velocity.app.VelocityEngine;
import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.reporting.service.ReportException;
import org.obiba.opal.reporting.service.ReportService;
import org.obiba.opal.shell.commands.options.ReportCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.google.common.annotations.VisibleForTesting;

@CommandUsage(description = "Generate a report based on the specified report template.", syntax = "Syntax: report --name TEMPLATE")
public class ReportCommand extends AbstractOpalRuntimeDependentCommand<ReportCommandOptions> {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(ReportCommand.class);

  private static final String DATE_FORMAT_PATTERN = "yyyyMMdd_HHmm";

  //
  // Instance Variables
  //

  @Autowired
  private ReportService reportService;

  @Autowired
  private MailSender mailSender;

  @Autowired
  private VelocityEngine velocityEngine;

  @Autowired
  @Value("${org.obiba.opal.public.url}")
  private String opalPublicUrl;

  @Autowired
  @Value("${org.obiba.opal.smtp.from}")
  private String fromAddress;

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
    Date reportDate = getCurrentTime();
    FileObject reportOutput = null;
    try {
      reportOutput = getReportOutput(reportTemplateName, reportTemplate.getFormat(), reportDate);
      reportService.render(reportTemplate.getFormat(), reportTemplate.getParameters(), getLocalFile(getReportDesign(reportTemplate.getDesign())).getPath(), getLocalFile(reportOutput).getPath());
    } catch(ReportException ex) {
      getShell().printf("Error rendering report: '%s'\n", ex.getMessage());
      deleteFileSilently(reportOutput);
      return 2;
    } catch(FileSystemException ex) {
      getShell().printf("Invalid report output destination: '/reports/%s/%s'", reportTemplateName, getReportFileName(reportTemplateName, reportTemplate.getFormat(), reportDate));
      return 3;
    }

    if(!reportTemplate.getEmailNotificationAddresses().isEmpty()) {
      sendEmailNotification(reportTemplate, reportOutput);
    }

    return 0;
  }

  //
  // Methods
  //

  public void setReportService(ReportService reportService) {
    this.reportService = reportService;
  }

  public void setMailSender(MailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void setOpalPublicUrl(String opalPublicUrl) {
    this.opalPublicUrl = opalPublicUrl;
  }

  public void setFromAddress(String fromAddress) {
    this.fromAddress = fromAddress;
  }

  @Override
  public String toString() {
    return "report -n " + getOptions().getName();
  }

  private FileObject getReportDesign(String reportDesign) throws FileSystemException {
    return getFile(reportDesign);
  }

  private FileObject getReportOutput(String reportTemplateName, String reportFormat, Date reportDate) throws FileSystemException {
    String reportFileName = getReportFileName(reportTemplateName, reportFormat, reportDate);

    FileObject reportDir = getFile("/reports/" + reportTemplateName);
    reportDir.createFolder();
    FileObject reportFile = reportDir.resolveFile(reportFileName);

    return reportFile;
  }

  private String getReportFileName(String reportTemplateName, String reportFormat, Date reportDate) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
    String reportDateText = dateFormat.format(reportDate);

    return reportTemplateName + "-" + reportDateText + "." + reportFormat;
  }

  private void deleteFileSilently(FileObject file) {
    try {
      if(file.exists()) {
        file.delete();
      }
    } catch(FileSystemException ex) {
      log.error("Could not delete file: {}", file.getName().getPath());
    }
  }

  private void sendEmailNotification(ReportTemplate reportTemplate, FileObject reportOutput) {
    String reportTemplateName = reportTemplate.getName();

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setSubject("[Opal] Report: " + reportTemplateName);
    message.setText(getEmailNotificationText(reportTemplateName, reportOutput));

    for(String emailAddress : reportTemplate.getEmailNotificationAddresses()) {
      message.setTo(emailAddress);
      try {
        mailSender.send(message);
      } catch(MailException ex) {
        getShell().printf("Email notification not sent: %s", ex.getMessage());
        log.error("Email notification not sent: {}", ex.getMessage());
      }
    }
  }

  private String getEmailNotificationText(String reportTemplateName, FileObject reportOutput) {
    Map<String, String> model = new HashMap<String, String>();
    model.put("report_template", reportTemplateName);
    model.put("report_public_link", opalPublicUrl + "/ws/report/public/" + getOpalRuntime().getFileSystem().getObfuscatedPath(reportOutput));

    return getMergedVelocityTemplate(model);
  }

  @VisibleForTesting
  String getMergedVelocityTemplate(Map<String, String> model) {
    return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "velocity/opal-reporting/report-email-notification.vm", model);
  }

  @VisibleForTesting
  Date getCurrentTime() {
    return new Date();
  }

}
