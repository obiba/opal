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

@Entity
@Table(name = "subject_acl")
public class SubjectAcl extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  @Index(name = "node_idx")
  private String node;

  @Column(nullable = false)
  @Index(name = "subject_idx")
  private String subject;

  @Column(nullable = false)
  private String permission;

  public SubjectAcl() {

  }

  public SubjectAcl(String node, String subject, String permission) {
    this.node = node;
    this.subject = subject;
    this.permission = permission;
  }

  public String getNode() {
    return node;
  }

  public String getSubject() {
    return subject;
  }

  public String getPermission() {
    return permission;
  }
}
