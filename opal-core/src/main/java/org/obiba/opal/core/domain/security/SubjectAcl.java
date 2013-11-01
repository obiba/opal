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

import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.obiba.opal.core.domain.AbstractTimestamped;
import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.service.SubjectAclService;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class SubjectAcl extends AbstractTimestamped implements HasUniqueProperties {

  @NotNull
  @NotBlank
  private String domain;

  @NotNull
  @NotBlank
  private String node;

  @NotNull
  @NotBlank
  private String principal;

  @NotNull
  @NotBlank
  private String type;

  @NotNull
  @NotBlank
  private String permission;

  public SubjectAcl() {

  }

  public SubjectAcl(@NotNull String domain, @NotNull String node, @NotNull SubjectAclService.Subject subject,
      @NotNull String permission) {
    this(domain, node, subject.getPrincipal(), subject.getType().toString(), permission);
  }

  private SubjectAcl(@NotNull String domain, @NotNull String node, @NotNull String principal, @NotNull String type,
      @NotNull String permission) {
    this.domain = domain;
    this.node = node;
    this.principal = principal;
    this.type = type;
    this.permission = permission;
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("domain", "node", "principal", "type", "permission");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.<Object>newArrayList(domain, node, principal, type, permission);
  }

  @NotNull
  public String getDomain() {
    return domain;
  }

  public void setDomain(@NotNull String domain) {
    this.domain = domain;
  }

  @NotNull
  public String getNode() {
    return node;
  }

  public void setNode(@NotNull String node) {
    this.node = node;
  }

  @NotNull
  public String getPermission() {
    return permission;
  }

  public void setPermission(@NotNull String permission) {
    this.permission = permission;
  }

  @NotNull
  public String getPrincipal() {
    return principal;
  }

  public void setPrincipal(@NotNull String principal) {
    this.principal = principal;
  }

  @NotNull
  public String getType() {
    return type;
  }

  public void setType(@NotNull String type) {
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
