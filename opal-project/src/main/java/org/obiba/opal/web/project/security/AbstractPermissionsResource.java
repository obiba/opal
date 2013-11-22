/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.project.security;

import java.util.List;

import org.obiba.opal.core.service.SubjectAclService;
import org.obiba.opal.web.support.InvalidRequestException;

import static org.obiba.opal.web.project.security.ProjectPermissionsResource.DOMAIN;

public abstract class AbstractPermissionsResource {

  protected void setPermission(List<String> principals, SubjectAclService.SubjectType type, String permission) {
    validatePrincipals(principals);

    for(String principal : principals) {
      SubjectAclService.Subject subject = type.subjectFor(principal);
      getSubjectAclService().deleteSubjectPermissions(DOMAIN, getNode(), subject);
      getSubjectAclService().addSubjectPermission(DOMAIN, getNode(), subject, permission);
    }
  }

  protected void deletePermissions(List<String> principals, SubjectAclService.SubjectType type) {
    validatePrincipals(principals);

    for(String principal : principals) {
      SubjectAclService.Subject subject = type.subjectFor(principal);
      getSubjectAclService().deleteSubjectPermissions(DOMAIN, getNode(), subject);
    }
  }

  private void validatePrincipals(List<String> principals) {
    if(principals == null || principals.isEmpty()) throw new InvalidRequestException("A principal is required.");
  }

  protected abstract SubjectAclService getSubjectAclService();

  protected abstract String getNode();

}
