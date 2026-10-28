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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal.AclAction;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.core.UriInfo;

public class OpalPermissions implements SubjectAclService.Permissions {

  private final URI uri;

  @Nullable
  private final String basePath;

  private final Iterable<String> perms;

  /**
   * Permissions on a resource URI that is relative to the web services root.
   */
  public OpalPermissions(URI uri, AclAction action) {
    this(null, uri, Lists.newArrayList(action));
  }

  /**
   * Permissions on a resource URI built from the base URI of the request, context path and /ws root included.
   */
  public OpalPermissions(UriInfo uriInfo, URI uri, AclAction action) {
    this(uriInfo.getBaseUri().getPath(), uri, Lists.newArrayList(action));
  }

  private OpalPermissions(@Nullable String basePath, URI uri, Iterable<AclAction> actions) {
    this.basePath = basePath;
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
    String path = uri.getPath();
    if (basePath == null) return path;
    return path.replaceFirst("^" + Pattern.quote(basePath.replaceAll("/+$", "")) + "(?=/|$)", "");
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