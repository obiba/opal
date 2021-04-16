/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security;

import java.util.Objects;

import org.apache.shiro.subject.Subject;
import org.obiba.opal.core.service.security.realm.BackgroundJobRealm;
import org.obiba.shiro.SessionStorageEvaluator;

public class OpalSessionStorageEvaluator extends SessionStorageEvaluator {

  @Override
  public boolean isSessionStorageEnabled(Subject subject) {
    return super.isSessionStorageEnabled(subject) &&
        !Objects.equals(subject.getPrincipal(), BackgroundJobRealm.SystemPrincipal.INSTANCE);
  }

}
