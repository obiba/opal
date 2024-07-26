/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.security;

import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.BaseResource;
import org.obiba.opal.web.support.InvalidRequestException;

import java.util.Collection;

import static org.obiba.opal.core.domain.security.SubjectAcl.SubjectType;

public abstract class AbstractPermissionsResource implements BaseResource {

  public static final String DOMAIN = "opal";

  protected void setPermission(Collection<String> principals, SubjectType type, String permission) {
    validatePrincipals(principals);

    for (String principal : principals) {
      SubjectAcl.Subject subject = type.subjectFor(principal);
      getSubjectAclService().deleteSubjectPermissions(DOMAIN, getNode(), subject);
      getSubjectAclService().addSubjectPermission(DOMAIN, getNode(), subject, permission);
    }
  }

  protected void deletePermissions(Collection<String> principals, SubjectType type) {
    validatePrincipals(principals);

    for (String principal : principals) {
      SubjectAcl.Subject subject = type.subjectFor(principal);
      getSubjectAclService().deleteSubjectPermissions(DOMAIN, getNode(), subject);
    }
  }

  private void validatePrincipals(Collection<String> principals) {
    if (principals == null || principals.isEmpty()) throw new InvalidRequestException("A principal is required.");
  }

  protected abstract SubjectAclService getSubjectAclService();

  protected abstract String getNode();

}
