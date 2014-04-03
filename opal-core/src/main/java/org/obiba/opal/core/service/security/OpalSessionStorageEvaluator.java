/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security;

import java.util.Objects;

import org.apache.shiro.mgt.SessionStorageEvaluator;
import org.apache.shiro.subject.Subject;
import org.obiba.opal.core.service.security.realm.BackgroundJobRealm;
import org.obiba.shiro.realm.SudoRealm;

public class OpalSessionStorageEvaluator implements SessionStorageEvaluator {

  @Override
  public boolean isSessionStorageEnabled(Subject subject) {
    if(subject == null) return false;
    Object principal = subject.getPrincipal();
    return !Objects.equals(principal, BackgroundJobRealm.SystemPrincipal.INSTANCE) &&
        !Objects.equals(principal, SudoRealm.SudoPrincipal.INSTANCE);
  }

}
