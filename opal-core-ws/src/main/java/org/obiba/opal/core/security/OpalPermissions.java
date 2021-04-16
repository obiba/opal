/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.security;

import java.net.URI;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal.AclAction;
import org.obiba.opal.web.ws.cfg.OpalWsConfig;

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
    perms = StreamSupport.stream(actions.spliterator(), false)
        .map(Enum::name).collect(Collectors.toList());
  }

  @Override
  public String getDomain() {
    return "opal";
  }

  @Override
  public String getNode() {
    return uri.getPath().replaceFirst(OpalWsConfig.WS_ROOT, "");
  }

  @Override
  public SubjectAcl.Subject getSubject() {
    return null;
  }

  @Override
  public Iterable<String> getPermissions() {
    return perms;
  }

}