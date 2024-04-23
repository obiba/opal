/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.shell.reporting;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.obiba.opal.core.domain.ReportTemplate;
import org.obiba.opal.core.service.ReportTemplateService;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;
import org.obiba.opal.web.reporting.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Scope("request")
@Path("/report-templates")
public class ReportTemplatesResource {

  private ReportTemplateService reportTemplateService;

  @Autowired
  public void setReportTemplateService(ReportTemplateService reportTemplateService) {
    this.reportTemplateService = reportTemplateService;
  }

  @GET
  public Set<ReportTemplateDto> get() {
    ImmutableSet.Builder<ReportTemplate> setBuilder = ImmutableSet.builder();
    setBuilder.addAll(Iterables.filter(reportTemplateService.getReportTemplates(), new Predicate<ReportTemplate>() {

      @Override
      public boolean apply(ReportTemplate template) {
        return ReportTemplateAuthorizer.authzGet(template);
      }
    }));
    return Dtos.asDto(setBuilder.build());
  }

}
