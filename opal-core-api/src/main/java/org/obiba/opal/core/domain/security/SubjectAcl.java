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

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.obiba.opal.core.domain.AbstractTimestamped;
import org.obiba.opal.core.domain.converter.SubjectTypeConverter;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * One permission granted to one subject on one node. Every access check in Opal reads this table, so its five
 * identifying columns are indexed individually as well as together.
 */
@Entity
@Table(name = "subject_acls",
    uniqueConstraints = @UniqueConstraint(name = "uk_subject_acls",
        columnNames = {"domain", "node", "principal", "type", "permission"}),
    indexes = {
        @Index(name = "idx_subject_acls_domain", columnList = "domain"),
        @Index(name = "idx_subject_acls_node", columnList = "node"),
        @Index(name = "idx_subject_acls_principal", columnList = "principal"),
        @Index(name = "idx_subject_acls_type", columnList = "type")
    })
public class SubjectAcl extends AbstractTimestamped {

  public enum SubjectType {

    USER, GROUP;

    public Subject subjectFor(@SuppressWarnings("ParameterHidesMemberVariable") String principal) {
      return new Subject(principal, this);
    }
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @NotBlank
  @Column(nullable = false)
  private String domain;

  /**
   * A resource path - down to a single variable - so it needs more room than a name.
   */
  @NotNull
  @NotBlank
  @Column(nullable = false, length = 1000)
  private String node;

  @NotNull
  @NotBlank
  @Column(nullable = false)
  private String principal;

  @NotNull
  @Convert(converter = SubjectTypeConverter.class)
  @Column(nullable = false)
  private SubjectType type;

  @NotNull
  @NotBlank
  @Column(nullable = false)
  private String permission;

  public SubjectAcl() {

  }

  public SubjectAcl(@NotNull String domain, @NotNull String node, @NotNull Subject subject,
                    @NotNull String permission) {
    this(domain, node, subject.getPrincipal(), subject.getType(), permission);
  }

  private SubjectAcl(@NotNull String domain, @NotNull String node, @NotNull String principal, @NotNull SubjectType type,
                     @NotNull String permission) {
    this.domain = domain;
    this.node = node;
    this.principal = principal;
    this.type = type;
    this.permission = permission;
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
  public SubjectType getType() {
    return type;
  }

  public void setType(@NotNull SubjectType type) {
    this.type = type;
  }

  public Subject getSubject() {
    return type.subjectFor(getPrincipal());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(domain, node, principal, type, permission);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    SubjectAcl other = (SubjectAcl) obj;
    return Objects.equal(domain, other.domain) && Objects.equal(node, other.node) &&
        Objects.equal(principal, other.principal) && Objects.equal(type, other.type) &&
        Objects.equal(permission, other.permission);
  }

  @Override
  public String toString() {
    return "[" + domain + ", " + node + ", " + principal + ", " + type + ", " + permission + "]";
  }

  public static class Subject implements Comparable<Subject>, Serializable {

    private static final long serialVersionUID = -4104563748622536925L;

    private final String principal;

    private final SubjectType type;

    public Subject(String principal, SubjectType type) {
      this.principal = principal;
      this.type = type;
    }

    public String getPrincipal() {
      return principal;
    }

    public SubjectType getType() {
      return type;
    }

    @Override
    public int compareTo(@NotNull Subject other) {
      return ComparisonChain.start() //
          .compare(type, other.type) //
          .compare(principal, other.principal) //
          .result();
    }

    @Override
    public String toString() {
      return getType() + ":" + getPrincipal();
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(principal, type);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      Subject other = (Subject) obj;
      return Objects.equal(principal, other.principal) && Objects.equal(type, other.type);
    }
  }
}
