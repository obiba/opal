/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security.authc;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpalSessionListener implements SessionListener {

  @Autowired
  private SubjectAclService subjectAclService;

  @Override
  public void onStart(Session session) {
  }

  @Override
  public void onStop(Session session) {
    subjectAclService.deleteNodePermissions("/auth/session/" + session.getId());
  }

  @Override
  public void onExpiration(Session session) {
    subjectAclService.deleteNodePermissions("/auth/session/" + session.getId());
  }

}
