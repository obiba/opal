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

import org.obiba.magma.security.Authorizer;
import org.obiba.magma.security.shiro.ShiroAuthorizer;
import org.obiba.opal.core.domain.ReportTemplate;

public class ReportTemplateAuthorizer {

  private final static Authorizer authorizer = new ShiroAuthorizer();

  private ReportTemplateAuthorizer() {}

  static boolean authzGet(ReportTemplate template) {
    return authorizer
        .isPermitted("rest:/project/" + template.getProject() + "/report-template/" + template.getName() + ":GET");
  }

}
