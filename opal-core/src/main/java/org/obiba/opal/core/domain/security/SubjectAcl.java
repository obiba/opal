/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.domain.security;

import javax.annotation.Nonnull;

import org.obiba.opal.core.domain.AbstractTimestamped;
import org.obiba.opal.core.service.SubjectAclService;

public class SubjectAcl extends AbstractTimestamped {

  @Nonnull
  private String domain;

  @Nonnull
  private String node;

  @Nonnull
  private String principal;

  @Nonnull
  private String type;

  @Nonnull
  private String permission;

  public SubjectAcl() {

  }

  public SubjectAcl(@Nonnull String domain, @Nonnull String node, @Nonnull SubjectAclService.Subject subject,
      @Nonnull String permission) {
    this(domain, node, subject.getPrincipal(), subject.getType().toString(), permission);
  }

  private SubjectAcl(@Nonnull String domain, @Nonnull String node, @Nonnull String principal, @Nonnull String type,
      @Nonnull String permission) {
    this.domain = domain;
    this.node = node;
    this.principal = principal;
    this.type = type;
    this.permission = permission;
  }

  @Nonnull
  public String getDomain() {
    return domain;
  }

  public void setDomain(@Nonnull String domain) {
    this.domain = domain;
  }

  @Nonnull
  public String getNode() {
    return node;
  }

  public void setNode(@Nonnull String node) {
    this.node = node;
  }

  @Nonnull
  public String getPermission() {
    return permission;
  }

  public void setPermission(@Nonnull String permission) {
    this.permission = permission;
  }

  @Nonnull
  public String getPrincipal() {
    return principal;
  }

  public void setPrincipal(@Nonnull String principal) {
    this.principal = principal;
  }

  @Nonnull
  public String getType() {
    return type;
  }

  public void setType(@Nonnull String type) {
    this.type = type;
  }

  public SubjectAclService.Subject getSubject() {
    return SubjectAclService.SubjectType.valueOf(getType()).subjectFor(getPrincipal());
  }
}
