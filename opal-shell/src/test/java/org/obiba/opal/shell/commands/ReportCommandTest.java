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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.commons.vfs.FileSystemException;
import org.junit.Test;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.reporting.service.ReportException;
import org.obiba.opal.reporting.service.ReportService;
import org.obiba.opal.shell.OpalShell;
import org.obiba.opal.shell.commands.options.ReportCommandOptions;
import org.obiba.opal.shell.test.support.OpalFileSystemMockBuilder;
import org.obiba.opal.shell.test.support.OpalRuntimeMockBuilder;
import org.obiba.opal.shell.test.support.OpalShellMockBuilder;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import com.google.common.collect.ImmutableSet;

/**
 * Unit tests for {@link ReportCommand}.
 */
public class ReportCommandTest {
  //
  // Test Methods
  //

  @Test
  public void testExecute_PrintsAnErrorIfReportTemplateDoesNotExist() throws FileSystemException, ReportException {
    // Setup
    String reportTemplateName = "bogusTemplate";
    ReportCommandOptions mockOptions = createMock(ReportCommandOptions.class);
    expect(mockOptions.getName()).andReturn(reportTemplateName).atLeastOnce();

    OpalShell opalShellMock = OpalShellMockBuilder.newBuilder().printf("Report template '%s' does not exist.\n", reportTemplateName).build();

    OpalConfiguration opalConfiguration = new OpalConfiguration();

    OpalRuntime opalRuntimeMock = OpalRuntimeMockBuilder.newBuilder().withOpalConfiguration(opalConfiguration).build();

    replay(mockOptions, opalShellMock, opalRuntimeMock);

    // Exercise
    ReportCommand sut = createReportCommand(opalRuntimeMock, opalConfiguration);
    sut.setOptions(mockOptions);
    sut.setShell(opalShellMock);

    int errorCode = sut.execute();

    // Verify behaviour
    verify(mockOptions, opalShellMock, opalRuntimeMock);

    // Verify state
    assertEquals(1, errorCode);
  }

  @Test
  public void testExecute_RendersReportWithoutEmailNotification() throws FileSystemException, ReportException {
    testExecute(ImmutableSet.<String> of());
  }

  @Test
  public void testExecute_RendersReportWithEmailNotification() throws FileSystemException, ReportException {
    testExecute(ImmutableSet.of("admin1@obiba.org", "admin2@obiba.org"));
  }

  //
  // Helper Methods
  //

  private void testExecute(Set<String> emailNotificationAddresses) throws FileSystemException, ReportException {
    // Setup
    ReportTemplate reportTemplate = createReportTemplate("testTemplate", "/testDesign.rptdesign", "pdf", emailNotificationAddresses);
    String reportDir = "/reports/" + reportTemplate.getName();
    String reportFileName = reportTemplate.getName() + "-" + "20100101_1300" + "." + reportTemplate.getFormat();
    String opalPublicUrl = "http://opal.obiba.org";

    ReportCommandOptions mockOptions = createMock(ReportCommandOptions.class);
    expect(mockOptions.getName()).andReturn(reportTemplate.getName()).atLeastOnce();

    OpalShell opalShellMock = OpalShellMockBuilder.newBuilder().build();

    OpalConfiguration opalConfiguration = new OpalConfiguration();
    opalConfiguration.addReportTemplate(reportTemplate);

    OpalFileSystem opalFileSystemMock = OpalFileSystemMockBuilder.newBuilder().resolveFile(reportTemplate.getDesign()).getLocalFile(reportTemplate.getDesign(), "/home/test" + reportTemplate.getDesign()).resolveFile(reportDir).createFolder(reportDir).once().resolveFile(reportDir, reportFileName).getLocalFile(reportDir + "/" + reportFileName, "/home/test" + reportDir + "/" + reportFileName).getObfuscatedPath(reportDir + "/" + reportFileName).build();

    OpalRuntime opalRuntimeMock = OpalRuntimeMockBuilder.newBuilder().withOpalConfiguration(opalConfiguration).withOpalFileSystem(opalFileSystemMock).build();

    ReportService reportServiceMock = createMock(ReportService.class);
    reportServiceMock.render(reportTemplate.getFormat(), reportTemplate.getParameters(), "/home/test" + reportTemplate.getDesign(), "/home/test" + reportDir + "/" + reportFileName);
    expectLastCall().once();

    MailSender mailSenderMock = createMock(MailSender.class);
    if(!emailNotificationAddresses.isEmpty()) {
      for(String emailAddress : reportTemplate.getEmailNotificationAddresses()) {
        mailSenderMock.send(createEmailNotification(reportTemplate.getName(), opalPublicUrl + "/ws/report/public/" + "OBFUSCATED[" + reportDir + "/" + reportFileName + "]", emailAddress));
      }
    }

    replay(mockOptions, opalShellMock, opalFileSystemMock, opalRuntimeMock, reportServiceMock, mailSenderMock);

    // Exercise
    ReportCommand sut = createReportCommand(opalRuntimeMock, opalConfiguration);
    sut.setOptions(mockOptions);
    sut.setShell(opalShellMock);
    sut.setReportService(reportServiceMock);
    sut.setMailSender(mailSenderMock);
    sut.setOpalPublicUrl(opalPublicUrl);

    int errorCode = sut.execute();

    // Verify behaviour
    verify(mockOptions, opalShellMock, opalFileSystemMock, opalRuntimeMock, reportServiceMock, mailSenderMock);

    // Verify state
    assertEquals(0, errorCode);
  }

  private ReportCommand createReportCommand(final OpalRuntime opalRuntime, final OpalConfiguration opalConfiguration) {
    return new ReportCommand() {
      @Override
      protected OpalRuntime getOpalRuntime() {
        return opalRuntime;
      }

      @Override
      protected OpalConfiguration getOpalConfiguration() {
        return opalConfiguration;
      }

      @Override
      Date getCurrentTime() {
        Calendar c = Calendar.getInstance();
        c.set(2010, 0, 1, 13, 0);

        return c.getTime();
      }

      @Override
      String getMergedVelocityTemplate(Map<String, String> model) {
        return model.get("report_template") + "," + model.get("report_public_link");
      }
    };
  }

  private ReportTemplate createReportTemplate(String name, String design, String format, Set<String> emailNotificationAddresses) {
    ReportTemplate reportTemplate = new ReportTemplate();

    reportTemplate.setName(name);
    reportTemplate.setDesign(design);
    reportTemplate.setFormat(format);
    reportTemplate.setEmailNotificationAddresses(emailNotificationAddresses);

    return reportTemplate;
  }

  private SimpleMailMessage createEmailNotification(String reportTemplateName, String reportObfuscatedPath, String emailAddress) {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setFrom("opal-mailer@obiba.org");
    msg.setTo(emailAddress);
    msg.setSubject("[Opal] Report: " + reportTemplateName);
    msg.setText(reportTemplateName + "," + reportObfuscatedPath);

    return msg;
  }
}
