/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.domain.ReportTemplate;
import org.obiba.opal.core.service.security.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.fest.assertions.api.Assertions.assertThat;

@ContextConfiguration(classes = ReportTemplateServiceImplTest.Config.class)
public class ReportTemplateServiceImplTest extends AbstractOrientdbServiceTest {

  @Autowired
  private ReportTemplateService reportTemplateService;

  @Autowired
  private OrientDbService orientDbService;

  @Override
  public void startDB() throws Exception {
    super.startDB();
    orientDbService.deleteAll(ReportTemplate.class);
  }

  @Test
  public void test_save() {
    ReportTemplate template = ReportTemplate.Builder.create() //
        .nameAndProject("template", "project") //
        .design("design") //
        .emailNotificationAddress("email1")//
        .emailNotificationAddress("email2")//
        .format("format") //
        .schedule("* * * * *") //
        .parameter("param1", "value1") //
        .parameter("param2", "value2") //
        .design("design") //
        .build();

    reportTemplateService.save(template);

    assertThat(reportTemplateService.getReportTemplates()).hasSize(1);
    ReportTemplate firstTemplate = reportTemplateService.getReportTemplates().iterator().next();
    assertEquals(firstTemplate, template);

    assertThat(reportTemplateService.getReportTemplates(template.getProject())).hasSize(1);
    assertEquals(reportTemplateService.getReportTemplates(template.getProject()).iterator().next(), template);

    assertThat(reportTemplateService.getReportTemplates("project2")).isEmpty();

    assertEquals(reportTemplateService.getReportTemplate(template.getName(), template.getProject()), template);

    assertThat(reportTemplateService.hasReportTemplate(template.getName(), template.getProject())).isTrue();
  }

  @Test(expected = NoSuchReportTemplateException.class)
  public void test_get_not_found() {
    reportTemplateService.getReportTemplate("template", "project");
  }

  @Test
  public void test_delete() {
    ReportTemplate template = ReportTemplate.Builder.create().nameAndProject("template", "project").build();
    reportTemplateService.save(template);

    reportTemplateService.delete(template.getName(), template.getProject());

    assertThat(reportTemplateService.hasReportTemplate(template.getName(), template.getProject())).isFalse();
  }

  @Test(expected = NoSuchReportTemplateException.class)
  public void test_delete_not_found() {
    reportTemplateService.delete("template", "project");
  }

  private void assertEquals(ReportTemplate template, ReportTemplate expected) {
    assertThat(template).isEqualTo(expected);
    assertThat(template.getName()).isEqualTo(expected.getName());
    assertThat(template.getProject()).isEqualTo(expected.getProject());
    assertThat(template.getDesign()).isEqualTo(expected.getDesign());
    assertThat(template.getFormat()).isEqualTo(expected.getFormat());
    assertThat(template.getSchedule()).isEqualTo(expected.getSchedule());
    assertThat(template.getEmailNotificationAddresses()).isEqualTo(expected.getEmailNotificationAddresses());
    assertThat(template.getParameters()).isEqualTo(expected.getParameters());
  }

  @Configuration
  public static class Config extends AbstractOrientDbTestConfig {

    @Bean
    public ReportTemplateService reportTemplateService() {
      return new ReportTemplateServiceImpl();
    }

    @Bean
    public CryptoService cryptoService() {
      return new CryptoService() {
        @Override
        public String generateSecretKey() {
          return null;
        }

        @Override
        public String encrypt(String plain) {
          return plain;
        }

        @Override
        public String decrypt(String encrypted) {
          return encrypted;
        }
      };
    }

  }
}
