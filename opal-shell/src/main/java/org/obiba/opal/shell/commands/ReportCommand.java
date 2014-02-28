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

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.velocity.app.VelocityEngine;
import org.obiba.opal.core.domain.ReportTemplate;
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
import com.google.common.collect.ImmutableMap;

@CommandUsage(description = "Generate a report based on the specified report template.",
    syntax = "Syntax: report --name TEMPLATE")
public class ReportCommand extends AbstractOpalRuntimeDependentCommand<ReportCommandOptions> {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(ReportCommand.class);

  private static final String DATE_FORMAT_PATTERN = "yyyyMMdd_HHmm";

  private static final Map<String, String> formatFileExtension = ImmutableMap
      .of("HTML", "html", "PDF", "pdf", "EXCEL", "xls");

  //
  // Instance Variables
  //

  @Autowired
  private ReportService reportService;

  @Autowired
  private MailSender mailSender;

  @Autowired
  private VelocityEngine velocityEngine;

  @Value("${org.obiba.opal.public.url}")
  private String opalPublicUrl;

  @Value("${org.obiba.opal.smtp.from}")
  private String fromAddress;

  //
  // AbstractOpalRuntimeDependentCommand Methods
  //

  @Override
  public int execute() {
    // Get the report template.
    String reportTemplateName = getOptions().getName();
    ReportTemplate reportTemplate = getOpalConfiguration().getReportTemplate(reportTemplateName);
    if(reportTemplate == null) {
      getShell().printf("Report template '%s' does not exist.\n", reportTemplateName);
      return 1;
    }

    Date reportDate = getCurrentTime();
    try {
      FileObject reportOutput = getReportOutput(reportTemplate, reportDate);
      return renderAndSendEmail(reportTemplate, reportOutput);
    } catch(FileSystemException e) {
      getShell().printf("Cannot create report output: '/reports/%s/%s'", reportTemplateName,
          getReportFileName(reportTemplateName, reportTemplate.getFormat(), reportDate));
      return 1;
    }

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

  private int renderAndSendEmail(ReportTemplate reportTemplate, FileObject reportOutput) throws FileSystemException {
    try {
      reportService.render(reportTemplate.getFormat(), reportTemplate.getParameters(),
          getLocalFile(getReportDesign(reportTemplate.getDesign())).getPath(), getLocalFile(reportOutput).getPath());
    } catch(ReportException ex) {
      getShell().printf("Error rendering report: '%s'\n", ex.getMessage());
      deleteFileSilently(reportOutput);
      return 1;
    }

    if(!reportTemplate.getEmailNotificationAddresses().isEmpty()) {
      sendEmailNotification(reportTemplate, reportOutput);
    }
    return 0;

  }

  private FileObject getReportDesign(String reportDesign) throws FileSystemException {
    return getFile(reportDesign);
  }

  private FileObject getReportOutput(ReportTemplate reportTemplate, Date reportDate) throws FileSystemException {
    String reportTemplateName = reportTemplate.getName();
    String reportFormat = reportTemplate.getFormat();
    String reportFileName = getReportFileName(reportTemplateName, reportFormat, reportDate);

    FileObject reportDir = getFile(
        "/reports/" + (reportTemplate.hasProject() ? reportTemplate.getProject() + "/" : "") + reportTemplateName);
    reportDir.createFolder();

    return reportDir.resolveFile(reportFileName);
  }

  private String getReportFileName(String reportTemplateName, String reportFormat, Date reportDate) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
    String reportDateText = dateFormat.format(reportDate);
    return String
        .format("%s-%s.%s", reportTemplateName, reportDateText, formatFileExtension.get(reportFormat.toUpperCase()));
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
    message.setText(getEmailNotificationText(reportTemplate, reportOutput));

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

  private String getEmailNotificationText(ReportTemplate reportTemplate, FileObject reportOutput) {
    Map<String, Object> model = new HashMap<>();
    model.put("report_template", reportTemplate.getName());
    model.put("report_public_link",
        opalPublicUrl + "/ws/report/public/" + getOpalRuntime().getFileSystem().getObfuscatedPath(reportOutput) +
            (reportTemplate.hasProject() ? "?project=" + reportTemplate.getProject() : ""));
    return getMergedVelocityTemplate(model);
  }

  @VisibleForTesting
  String getMergedVelocityTemplate(Map<String, Object> model) {
    return VelocityEngineUtils
        .mergeTemplateIntoString(velocityEngine, "velocity/opal-reporting/report-email-notification.vm", "UTF-8",
            model);
  }

  @VisibleForTesting
  Date getCurrentTime() {
    return new Date();
  }

}
