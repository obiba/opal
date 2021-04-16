/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.install;

import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.InstallStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To be consistent with Agate default settings.
 */
public class DefaultPermissionsInstallStep implements InstallStep {

  private static final Logger log = LoggerFactory.getLogger(DefaultPermissionsInstallStep.class);

  private SubjectAclService subjectAclService;

  @Override
  public String getDescription() {
    return "Apply default permissions";
  }

  @Override
  public void execute(Version currentVersion) {
    applyPermission("opal", "/", SubjectAcl.SubjectType.GROUP.subjectFor("opal-administrator"), "SYSTEM_ALL");
  }

  public void setSubjectAclService(SubjectAclService subjectAclService) {
    this.subjectAclService = subjectAclService;
  }

  private void applyPermission(String domain, String node, SubjectAcl.Subject subject, String permission) {
    if (subjectAclService == null) return;
    try {
      log.info("Applying default permission: {}:{}:{} => {}", domain, node, subject, permission);
      subjectAclService.addSubjectPermission(domain, node, subject, permission);
    } catch (Exception e) {
      log.warn("Enable to apply default permission: {}:{}:{} => {}", domain, node, subject, permission, e);
    }
  }
}
