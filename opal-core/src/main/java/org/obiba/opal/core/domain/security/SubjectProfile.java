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

public class SubjectProfile extends AbstractTimestamped implements HasUniqueProperties {

  @NotNull
  @NotBlank
  private String principal;

  @NotNull
  @NotBlank
  private String realm;

  public SubjectProfile() {

  }

  public SubjectProfile(@NotNull String principal, @NotNull String realm) {
    this.principal = principal;
    this.realm = realm;
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("principal");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.<Object>newArrayList(principal);
  }

  @NotNull
  public String getPrincipal() {
    return principal;
  }

  public void setPrincipal(@NotNull String principal) {
    this.principal = principal;
  }

  @NotNull
  public String getRealm() {
    return realm;
  }

  public void setRealm(@NotNull String realm) {
    this.realm = realm;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(principal, realm);
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null || getClass() != obj.getClass()) {
      return false;
    }
    SubjectProfile other = (SubjectProfile) obj;
    return Objects.equal(principal, other.principal) && Objects.equal(realm, other.realm);
  }

  public static class Builder {
    SubjectProfile profile;

    private Builder() {
    }

    public static Builder create() {
      Builder builder = new Builder();
      builder.profile = new SubjectProfile();
      return builder;
    }

    public static Builder create(String principal) {
      Builder builder = create();
      builder.principal(principal);
      return builder;
    }

    public Builder principal(String principal) {
      profile.setPrincipal(principal);
      return this;
    }

    public Builder realm(String realm) {
      profile.setRealm(realm);
      return this;
    }

    public SubjectProfile build() {
      return profile;
    }
  }
}
