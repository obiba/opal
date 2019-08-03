/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain.security;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.hibernate.validator.constraints.NotBlank;
import org.obiba.opal.core.domain.AbstractTimestamped;
import org.obiba.opal.core.domain.HasUniqueProperties;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is a Personal API access token: allow a tier to connect to Opal on behalf of a subject and with restricted permissions.
 */
public class SubjectToken extends AbstractTimestamped implements HasUniqueProperties {

  /**
   * The token string.
   */
  @NotNull
  @NotBlank
  private String token;

  /**
   * The associated subject's principal.
   */
  @NotNull
  @NotBlank
  private String principal;

  /**
   * A human friendly name.
   */
  @NotNull
  @NotBlank
  private String name;

  private Set<String> projects;

  public SubjectToken() {
  }

  public SubjectToken(@NotNull String token, @NotNull String principal, @NotNull String name) {
    this.token = token;
    this.principal = principal;
    this.name = name;
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("token");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.newArrayList(token);
  }

  @NotNull
  public String getPrincipal() {
    return principal;
  }

  public void setPrincipal(@NotNull String principal) {
    this.principal = principal;
  }

  @NotNull
  public String getToken() {
    return token;
  }

  public void setToken(@NotNull String token) {
    this.token = token;
  }

  public boolean hasToken() {
    return !Strings.isNullOrEmpty(token);
  }

  @NotNull
  public String getName() {
    return name;
  }

  public void setName(@NotNull String name) {
    this.name = name;
  }

  public Set<String> getProjects() {
    return projects == null ? projects = new HashSet<>() : projects;
  }

  public void addAllProjects(Collection<String> projects) {
    getProjects().addAll(projects);
  }

  public void setProjects(Set<String> projects) {
    this.projects = projects;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(principal, token, name);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    SubjectToken other = (SubjectToken) obj;
    return Objects.equal(principal, other.principal) && Objects.equal(token, other.token) && Objects.equal(name, other.name);
  }

}
