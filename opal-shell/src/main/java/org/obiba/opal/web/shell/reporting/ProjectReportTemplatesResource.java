/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.shell.reporting;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.obiba.opal.core.domain.ReportTemplate;
import org.obiba.opal.core.service.ReportTemplateService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.reporting.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@Component
@Transactional
@Scope("request")
@Path("/project/{name}/report-templates")
public class ProjectReportTemplatesResource {

  @PathParam("name")
  private String name;

  private ReportTemplateService reportTemplateService;

  @Autowired
  public void setReportTemplateService(ReportTemplateService reportTemplateService) {
    this.reportTemplateService = reportTemplateService;
  }

  @GET
  public Set<Opal.ReportTemplateDto> get() {
    ImmutableSet.Builder<ReportTemplate> setBuilder = ImmutableSet.builder();
    setBuilder.addAll(Iterables.filter(reportTemplateService.getReportTemplates(name), new Predicate<ReportTemplate>() {
      @Override
      public boolean apply(ReportTemplate template) {
        return ReportTemplateAuthorizer.authzGet(template);
      }
    }));
    return Dtos.asDto(setBuilder.build());
  }

}
