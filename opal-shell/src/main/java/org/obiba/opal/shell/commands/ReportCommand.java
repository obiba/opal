/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.shell.commands;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.obiba.opal.core.domain.ReportTemplate;
import org.obiba.opal.core.service.NoSuchReportTemplateException;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.core.service.ReportTemplateService;
import org.obiba.opal.reporting.service.ReportService;
import org.obiba.opal.shell.commands.options.ReportCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@CommandUsage(description = "Generate a report based on the specified report template.",
    syntax = "Syntax: report --project PROJECT --name TEMPLATE")
public class ReportCommand extends AbstractOpalRuntimeDependentCommand<ReportCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(ReportCommand.class);

  private static final String DATE_FORMAT_PATTERN = "yyyyMMdd_HHmm";

  private static final Map<String, String> formatFileExtension = ImmutableMap
      .of("HTML", "html", "PDF", "pdf", "EXCEL", "xls");

  @Autowired
  private OpalGeneralConfigService configService;

  @Autowired
  private ReportService reportService;

  @Autowired
  private ReportTemplateService reportTemplateService;

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
    getShell().progress("Prepare report", 0, 3, 0);
    // Get the report template.
    String name = getOptions().getName();
    ReportTemplate reportTemplate;
    try {
      reportTemplate = reportTemplateService.getReportTemplate(name, getOptions().getProject());
    } catch (NoSuchReportTemplateException e) {
      getShell().printf("Report template '%s' does not exist.\n", name);
      return 1;
    }

    Date reportDate = getCurrentTime();
    try {
      FileObject reportOutput = getReportOutput(reportTemplate, reportDate);
      return renderAndSendEmail(reportTemplate, reportOutput);
    } catch (FileSystemException e) {
      getShell().printf("Cannot create report output: '/reports/%s/%s'", name,
          getReportFileName(name, reportTemplate.getFormat(), reportDate));
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
    return "report -n \"" + getOptions().getName() + "\" -p \"" + getOptions().getProject() + "\"";
  }

  private int renderAndSendEmail(ReportTemplate reportTemplate, FileObject reportOutput) throws FileSystemException {
    getShell().progress("Executing report", 1, 3, 10);
    try {
      reportService.render(reportTemplate.getFormat(), reportTemplate.getParameters(),
          getLocalFile(getReportDesign(reportTemplate.getDesign())).getPath(), getLocalFile(reportOutput).getPath());
    } catch (Exception ex) {
      getShell().progress("Sending emails about failure", 2, 3, 90);
      String message = ex.getMessage();
      sendFailureEmailNotification(reportTemplate, message);
      getShell().printf("Error rendering report: '%s'\n", ex.getMessage());
      deleteFileSilently(reportOutput);
      return 1;
    }

    getShell().progress("Sending emails", 2, 3, 90);
    sendEmailNotification(reportTemplate, reportOutput);
    getShell().progress(reportTemplate.getName(), 3, 3, 100);
    return 0;

  }

  private FileObject getReportDesign(String reportDesign) throws FileSystemException {
    return getFile(reportDesign);
  }

  private FileObject getReportOutput(ReportTemplate reportTemplate, Date reportDate) throws FileSystemException {
    String reportTemplateName = reportTemplate.getName();
    String reportFormat = reportTemplate.getFormat();
    String reportFileName = getReportFileName(reportTemplateName, reportFormat, reportDate);

    FileObject reportDir = getFile("/reports/" + reportTemplate.getProject() + "/" + reportTemplateName);
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
      if (file.exists()) {
        file.delete();
      }
    } catch (FileSystemException ex) {
      log.error("Could not delete file: {}", file.getName().getPath());
    }
  }

  private void sendEmailNotification(ReportTemplate reportTemplate, FileObject reportOutput) {
    if (reportTemplate.getEmailNotificationAddresses().isEmpty()) return;

    String reportTemplateName = reportTemplate.getName();

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setSubject(String.format("[%s] %s", configService.getConfig().getName(), reportTemplateName));
    message.setText(getEmailNotificationText(reportTemplate, reportOutput));

    for (String emailAddress : reportTemplate.getEmailNotificationAddresses()) {
      message.setTo(emailAddress);
      try {
        mailSender.send(message);
      } catch (MailException ex) {
        getShell().printf("Email notification not sent: %s", ex.getMessage());
        log.error("Email notification not sent: {}", ex.getMessage());
      }
    }
  }

  private void sendFailureEmailNotification(ReportTemplate reportTemplate, String errorMessage) {
    if (reportTemplate.getFailureEmailNotificationAddresses().isEmpty()) return;

    String reportTemplateName = reportTemplate.getName();

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setSubject(String.format("[%s] %s", configService.getConfig().getName(), reportTemplateName));
    message.setText(getFailureEmailNotificationText(reportTemplate, errorMessage));

    for (String emailAddress : reportTemplate.getFailureEmailNotificationAddresses()) {
      message.setTo(emailAddress);
      try {
        mailSender.send(message);
      } catch (MailException ex) {
        getShell().printf("Failure email notification not sent: %s", ex.getMessage());
        log.error("Failure email notification not sent: {}", ex.getMessage());
      }
    }
  }

  private String getEmailNotificationText(ReportTemplate reportTemplate, FileObject reportOutput) {
    Map<String, Object> model = new HashMap<>();
    model.put("report_template", reportTemplate.getName());
    model.put("report_public_link",
        opalPublicUrl + "/ws/report/public/" + getOpalFileSystemService().getFileSystem().getObfuscatedPath(reportOutput) +
            "?project=" + reportTemplate.getProject());
    return getMergedVelocityTemplate("report-email-notification.vm", model);
  }

  private String getFailureEmailNotificationText(ReportTemplate reportTemplate, String errorMessage) {
    Map<String, Object> model = new HashMap<>();
    model.put("report_template", reportTemplate.getName());
    model.put("report_error", errorMessage);
    return getMergedVelocityTemplate("report-failure-email-notification.vm", model);
  }

  @VisibleForTesting
  String getMergedVelocityTemplate(String templateName, Map<String, Object> model) {
    StringWriter result = new StringWriter();
    VelocityContext velocityContext = new VelocityContext(model);
    velocityEngine.mergeTemplate(String.format("velocity/opal-reporting/%s", templateName), "UTF-8", velocityContext, result);
    return result.toString();
  }

  @VisibleForTesting
  Date getCurrentTime() {
    return new Date();
  }



}
