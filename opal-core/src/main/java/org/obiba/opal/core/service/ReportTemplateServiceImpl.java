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

import org.obiba.opal.core.domain.ReportTemplate;
import org.obiba.opal.core.service.security.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class ReportTemplateServiceImpl implements ReportTemplateService {

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private CryptoService cryptoService;

  @Override
  public void start() {
    orientDbService.createUniqueIndex(ReportTemplate.class);
  }

  @Override
  public void stop() {
  }

  @NotNull
  @Override
  public Iterable<ReportTemplate> getReportTemplates() {
    return StreamSupport.stream(orientDbService.list(ReportTemplate.class)
        .spliterator(), false)
        .map(this::decryptParameters)
        .collect(Collectors.toList());
  }

  @NotNull
  @Override
  public Iterable<ReportTemplate> getReportTemplates(@NotNull String project) {
    return StreamSupport.stream(orientDbService
        .list(ReportTemplate.class, "select from " + ReportTemplate.class.getSimpleName() + " where project = ?",
            project)
        .spliterator(), false)
        .map(this::decryptParameters)
        .collect(Collectors.toList());
  }

  @NotNull
  @Override
  public ReportTemplate getReportTemplate(@NotNull String name, @NotNull String project)
      throws NoSuchReportTemplateException {
    ReportTemplate reportTemplate = orientDbService.findUnique(new ReportTemplate(name, project));
    if (reportTemplate == null) throw new NoSuchReportTemplateException(name, project);
    return decryptParameters(reportTemplate);
  }

  @Override
  public boolean hasReportTemplate(@NotNull String name, @NotNull String project) {
    try {
      getReportTemplate(name, project);
      return true;
    } catch (NoSuchReportTemplateException e) {
      return false;
    }
  }

  @Override
  public void save(@NotNull ReportTemplate reportTemplate) throws ConstraintViolationException {
    ReportTemplate toSave = encryptParameters(reportTemplate);
    orientDbService.save(toSave, toSave);
  }

  @Override
  public void delete(@NotNull String name, @NotNull String project) throws NoSuchReportTemplateException {
    orientDbService.delete(getReportTemplate(name, project));
  }

  private ReportTemplate encryptParameters(ReportTemplate reportTemplate) {
    ReportTemplate template = reportTemplate;
    if (reportTemplate.hasParameters()) {
      ReportTemplate reportTemplateCopy = ReportTemplate.Builder.copy(reportTemplate).build();
      String jsonParams = orientDbService.toJson(reportTemplate.getParameters());
      reportTemplateCopy.setEncryptedParameters(cryptoService.encrypt(jsonParams));
      template = reportTemplateCopy;
    }
    return template;
  }

  private ReportTemplate decryptParameters(ReportTemplate reportTemplate) {
    if (reportTemplate.hasEncryptedParameters()) {
      String jsonParams = cryptoService.decrypt(reportTemplate.getEncryptedParameters());
      reportTemplate.setParameters(orientDbService.fromJson(jsonParams, Map.class));
    }
    return reportTemplate;
  }
}
