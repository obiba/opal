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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.obiba.core.domain.AbstractEntity;
import org.obiba.opal.core.service.SubjectAclService;

@Entity
@Table(name = "subject_acl")
public class SubjectAcl extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  @Index(name = "domain_idx")
  private String domain;

  @Column(nullable = false)
  @Index(name = "node_idx")
  private String node;

  @Column(nullable = false)
  @Index(name = "principal_idx")
  private String principal;

  @Column(nullable = false)
  @Index(name = "type_idx")
  private String type;

  @Column(nullable = false)
  private String permission;

  public SubjectAcl() {

  }

  public SubjectAcl(String domain) {
    this(domain, null);
  }

  public SubjectAcl(SubjectAclService.Subject subject) {
    this(null, null, subject);
  }

  public SubjectAcl(String domain, String node) {
    this(domain, node, null, null, null);
  }

  public SubjectAcl(String domain, String node, SubjectAclService.Subject subject) {
    this(domain, node, subject.getPrincipal(), subject.getType().toString(), null);
  }

  public SubjectAcl(String domain, String node, SubjectAclService.Subject subject, String permission) {
    this(domain, node, subject.getPrincipal(), subject.getType().toString(), permission);
  }

  private SubjectAcl(String domain, String node, String principal, String type, String permission) {
    this.domain = domain;
    this.node = node;
    this.principal = principal;
    this.type = type;
    this.permission = permission;
  }

  public String getDomain() {
    return domain;
  }

  public String getNode() {
    return node;
  }

  public String getPrincipal() {
    return principal;
  }

  public String getPermission() {
    return permission;
  }

  public String getType() {
    return type;
  }

  public SubjectAclService.Subject getSubject() {
    return SubjectAclService.SubjectType.valueOf(getType()).subjectFor(getPrincipal());
  }
}
