/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.domain.security;

import com.google.common.collect.Lists;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.obiba.opal.core.domain.AbstractTimestamped;
import org.obiba.opal.core.domain.converter.AuthenticationTypeConverter;
import org.obiba.opal.core.domain.converter.StringSetConverter;
import org.obiba.opal.core.validator.NotNullIfAnotherFieldHasValue;

import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NotNullIfAnotherFieldHasValue.List(
    @NotNullIfAnotherFieldHasValue(fieldName = "authenticationType", fieldValue = "PASSWORD",
        dependFieldName = "password"))
@Entity
@Table(name = "subject_credentials",
    uniqueConstraints = @UniqueConstraint(name = "uk_subject_credentials_name", columnNames = "name"))
public class SubjectCredentials extends AbstractTimestamped
    implements Comparable<SubjectCredentials> {

  public enum AuthenticationType {
    PASSWORD, CERTIFICATE
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @NotBlank
  @Column(nullable = false)
  private String name;

  @NotNull
  @Convert(converter = AuthenticationTypeConverter.class)
  @Column(name = "authentication_type")
  private AuthenticationType authenticationType;

  private String password; // for user only

  /**
   * The certificate itself lives in the keystore, not here; this field only carries it between the request and the
   * keystore service, which is why it has never been stored.
   */
  @Transient
  @SuppressWarnings("TransientFieldInNonSerializableClass")
  private transient byte[] certificate; // for application only

  /**
   * alias used to store certificate into KeyStore
   */
  @Column(name = "certificate_alias")
  private String certificateAlias;

  @Column(nullable = false)
  private boolean enabled;

  @Lob
  @Convert(converter = StringSetConverter.class)
  @Column(name = "group_names")
  private Set<String> groups = new HashSet<>();

  public SubjectCredentials() {
  }

  public SubjectCredentials(@NotNull String name) {
    this.name = name;
  }

  public String generateCertificateAlias() {
    return name.toLowerCase().replaceAll("[^A-Za-z0-9]", "") + "-" + System.currentTimeMillis();
  }

  @NotNull
  public String getName() {
    return name;
  }

  public void setName(@NotNull String name) {
    this.name = name;
  }

  @NotNull
  public AuthenticationType getAuthenticationType() {
    return authenticationType;
  }

  public void setAuthenticationType(@NotNull AuthenticationType authenticationType) {
    this.authenticationType = authenticationType;
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

  public String getCertificateAlias() {
    return certificateAlias;
  }

  public void setCertificateAlias(String certificateAlias) {
    this.certificateAlias = certificateAlias;
  }

  public Set<String> getGroups() {
    return groups;
  }

  public void setGroups(Set<String> groups) {
    this.groups = groups;
  }

  public void addGroup(String group) {
    if (groups == null) groups = new HashSet<>();
    groups.add(group);
  }

  public boolean hasGroup(String group) {
    return groups != null && groups.contains(group);
  }

  public void removeGroup(String group) {
    if (groups != null) groups.remove(group);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SubjectCredentials)) return false;
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

    public Builder authenticationType(AuthenticationType authenticationType) {
      subjectCredentials.authenticationType = authenticationType;
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
