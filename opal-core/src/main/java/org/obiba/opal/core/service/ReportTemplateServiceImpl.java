/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;

import org.obiba.opal.core.domain.ReportTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReportTemplateServiceImpl implements ReportTemplateService {

  @Autowired
  private OrientDbService orientDbService;

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(ReportTemplate.class);
  }

  @Override
  @PreDestroy
  public void stop() {}

  @NotNull
  @Override
  public Iterable<ReportTemplate> getReportTemplates() {
    return orientDbService.list(ReportTemplate.class);
  }

  @NotNull
  @Override
  public Iterable<ReportTemplate> getReportTemplates(@NotNull String project) {
    return orientDbService
        .list(ReportTemplate.class, "select from " + ReportTemplate.class.getSimpleName() + " where project = ?",
            project);
  }

  @NotNull
  @Override
  public ReportTemplate getReportTemplate(@NotNull String name, @NotNull String project)
      throws NoSuchReportTemplateException {
    ReportTemplate reportTemplate = orientDbService.findUnique(new ReportTemplate(name, project));
    if(reportTemplate == null) throw new NoSuchReportTemplateException(name, project);
    return reportTemplate;
  }

  @Override
  public boolean hasReportTemplate(@NotNull String name, @NotNull String project) {
    try {
      getReportTemplate(name, project);
      return true;
    } catch(NoSuchProjectException e) {
      return false;
    }
  }

  @Override
  public void save(@NotNull ReportTemplate reportTemplate) throws ConstraintViolationException {
    orientDbService.save(reportTemplate, reportTemplate);
  }

  @Override
  public void delete(@NotNull String name, @NotNull String project) throws NoSuchReportTemplateException {
    orientDbService.delete(getReportTemplate(name, project));
  }

}
