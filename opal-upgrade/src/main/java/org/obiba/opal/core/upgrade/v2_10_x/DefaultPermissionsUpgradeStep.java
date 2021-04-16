/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.v2_10_x;

import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultPermissionsUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(DefaultPluginsUpgradeStep.class);

  @Autowired
  private SubjectAclService subjectAclService;

  @Override
  public void execute(Version currentVersion) {
    applyPermission("opal", "/", SubjectAcl.SubjectType.GROUP.subjectFor("opal-administrator"), "SYSTEM_ALL");
  }

  private void applyPermission(String domain, String node, SubjectAcl.Subject subject, String permission) {
    if (subjectAclService == null) return;
    try {
      SubjectAclService.Permissions perms = subjectAclService.getSubjectNodePermissions(domain, node, subject);
      // skip if a permission was already set
      if (perms.getPermissions().iterator().hasNext()) return;
      log.info("Applying default permission: {}:{}:{} => {}", domain, node, subject, permission);
      subjectAclService.addSubjectPermission(domain, node, subject, permission);
    } catch (Exception e) {
      log.warn("Enable to apply default permission: {}:{}:{} => {}", domain, node, subject, permission, e);
    }
  }
}
