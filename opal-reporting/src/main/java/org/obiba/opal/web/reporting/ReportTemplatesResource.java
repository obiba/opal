/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.reporting;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/report-templates")
public class ReportTemplatesResource {

  private static final Logger log = LoggerFactory.getLogger(ReportTemplatesResource.class);

  private final OpalRuntime opalRuntime;

  @Autowired
  public ReportTemplatesResource(OpalRuntime opalRuntime) {
    super();
    this.opalRuntime = opalRuntime;
  }

  @GET
  public Set<ReportTemplateDto> getReportTemplates() {
    Set<ReportTemplate> templates = opalRuntime.getOpalConfiguration().getReportTemplates();
    return Dtos.asDto(templates);
  }

}
