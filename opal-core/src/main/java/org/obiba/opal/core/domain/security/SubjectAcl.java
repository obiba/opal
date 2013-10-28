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

import org.hibernate.validator.constraints.NotBlank;
import org.obiba.opal.core.domain.AbstractTimestamped;
import org.obiba.opal.core.service.SubjectAclService;

import com.google.common.base.Objects;

public class SubjectAcl extends AbstractTimestamped {

  @Nonnull
  @NotBlank
  private String domain;

  @Nonnull
  @NotBlank
  private String node;

  @Nonnull
  @NotBlank
  private String principal;

  @Nonnull
  @NotBlank
  private String type;

  @Nonnull
  @NotBlank
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

  @Override
  public int hashCode() {
    return Objects.hashCode(domain, node, principal, type, permission);
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null || getClass() != obj.getClass()) {
      return false;
    }
    SubjectAcl other = (SubjectAcl) obj;
    return Objects.equal(domain, other.domain) && Objects.equal(node, other.node) &&
        Objects.equal(principal, other.principal) && Objects.equal(type, other.type) &&
        Objects.equal(permission, other.permission);
  }
}
