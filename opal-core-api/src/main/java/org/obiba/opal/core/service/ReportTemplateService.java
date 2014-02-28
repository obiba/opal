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

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;

import org.obiba.opal.core.domain.ReportTemplate;

/**
 * Service to manage report templates.
 */
public interface ReportTemplateService extends SystemService {

  @NotNull
  Iterable<ReportTemplate> getReportTemplates();

  @NotNull
  Iterable<ReportTemplate> getReportTemplates(@NotNull String project);

  @NotNull
  ReportTemplate getReportTemplate(@NotNull String name, @NotNull String project) throws NoSuchReportTemplateException;

  boolean hasReportTemplate(@NotNull String name, @NotNull String project);

  void save(@NotNull ReportTemplate reportTemplate) throws ConstraintViolationException;

  void delete(@NotNull String name, @NotNull String project) throws NoSuchReportTemplateException;

}
