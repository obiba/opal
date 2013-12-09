/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.domain.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.shiro.crypto.hash.Sha256Hash;
import org.hibernate.validator.constraints.NotBlank;
import org.obiba.core.util.HexUtil;
import org.obiba.opal.core.domain.AbstractTimestamped;
import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.validator.NotNullIfAnotherFieldHasValue;

import com.google.common.collect.Lists;

@NotNullIfAnotherFieldHasValue.List({ //
    @NotNullIfAnotherFieldHasValue(fieldName = "type", fieldValue = "USER", dependFieldName = "password"), //
    @NotNullIfAnotherFieldHasValue(fieldName = "type", fieldValue = "APPLICATION", dependFieldName = "certificate") })
public class SubjectCredentials extends AbstractTimestamped
    implements Comparable<SubjectCredentials>, HasUniqueProperties {

  public enum Status {
    ACTIVE, INACTIVE
  }

  public enum Type {
    USER, APPLICATION
  }

  @NotNull
  @NotBlank
  private String name;

  @NotNull
  private Type type;

  // user password
  private String password;

  // application certificate
  private byte[] certificate;

  private boolean enabled;

  private Set<String> groups = new HashSet<String>();

  public SubjectCredentials() {
  }

  public SubjectCredentials(@NotNull String name) {
    this.name = name;
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("name");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.<Object>newArrayList(name);
  }

  @NotNull
  public String getName() {
    return name;
  }

  public void setName(@NotNull String name) {
    this.name = name;
  }

  @NotNull
  public Type getType() {
    return type;
  }

  public void setType(@NotNull Type type) {
    this.type = type;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public byte[] getCertificate() {
    return certificate;
  }

  public void setCertificate(byte... certificate) {
    this.certificate = certificate;
  }

  public Set<String> getGroups() {
    return groups;
  }

  public void setGroups(Set<String> groups) {
    this.groups = groups;
  }

  public void addGroup(String group) {
    if(groups == null) groups = new HashSet<String>();
    groups.add(group);
  }

  public boolean hasGroup(String group) {
    return groups != null && groups.contains(group);
  }

  public void removeGroup(String group) {
    if(groups != null) groups.remove(group);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  // TODO use Shiro to digest password
  public static String digest(String password, byte... salt) {
    try {
      MessageDigest digest = MessageDigest.getInstance(Sha256Hash.ALGORITHM_NAME);
      digest.reset();
      digest.update(salt);
      return HexUtil.bytesToHex(digest.digest(password.getBytes()));
    } catch(NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(!(o instanceof SubjectCredentials)) return false;
    SubjectCredentials subjectCredentials = (SubjectCredentials) o;
    return name.equals(subjectCredentials.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public int compareTo(@NotNull SubjectCredentials subjectCredentials) {
    return name.compareTo(subjectCredentials.name);
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private SubjectCredentials subjectCredentials;

    private Builder() {
    }

    public static Builder create() {
      Builder builder = new Builder();
      builder.subjectCredentials = new SubjectCredentials();
      return builder;
    }

    public Builder name(String name) {
      subjectCredentials.name = name;
      return this;
    }

    public Builder type(Type type) {
      subjectCredentials.type = type;
      return this;
    }

    public Builder password(String password) {
      subjectCredentials.password = password;
      return this;
    }

    public Builder certificate(byte... certificate) {
      subjectCredentials.certificate = certificate;
      return this;
    }

    public Builder enabled(boolean enabled) {
      subjectCredentials.enabled = enabled;
      return this;
    }

    public Builder groups(Set<String> groups) {
      subjectCredentials.groups = groups;
      return this;
    }

    public Builder group(String group) {
      subjectCredentials.addGroup(group);
      return this;
    }

    public SubjectCredentials build() {
      return subjectCredentials;
    }
  }

}
