/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.security.support;

import java.net.URI;

import org.obiba.opal.core.service.SubjectAclService;
import org.obiba.opal.core.service.SubjectAclService.Subject;
import org.obiba.opal.web.model.Opal.AclAction;
import org.obiba.opal.web.ws.cfg.ResteasyServletConfiguration;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class OpalPermissions implements SubjectAclService.Permissions {

  private final URI uri;

  private final Iterable<String> perms;

  public OpalPermissions(URI uri, AclAction action) {
    this(uri, Lists.newArrayList(action));
  }

  public OpalPermissions(URI uri, Iterable<AclAction> actions) {
    this.uri = uri;
    perms = Iterables.transform(actions, new Function<AclAction, String>() {

      @Override
      public String apply(AclAction input) {
        return input.toString();
      }

    });
  }

  @Override
  public String getDomain() {
    return "opal";
  }

  @Override
  public String getNode() {
    return uri.getPath().replaceFirst(ResteasyServletConfiguration.WS_ROOT, "");
  }

  @Override
  public Subject getSubject() {
    return null;
  }

  @Override
  public Iterable<String> getPermissions() {
    return perms;
  }

}