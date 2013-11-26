/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public interface SubjectAclService extends SystemService {

  /**
   * Add permission change callback.
   * @param callback
   */
  void addListener(SubjectAclChangeCallback callback);

  /**
   * Delete all permissions starting with node.
   *
   * @param node
   */
  void deleteNodePermissions(String node);

  /**
   * Delete all permissions matching exactly node in a domain.
   *
   * @param domain
   * @param node
   */
  void deleteNodePermissions(String domain, String node);

  /**
   * Delete all permissions on node and its children in a domain.
   * @param domain
   * @param node
   */
  void deleteNodeHierarchyPermissions(String domain, String node);

  /**
   * Delete all node permissions of a subject in a domain.
   * @param domain
   * @param node
   * @param subject
   */
  void deleteSubjectPermissions(String domain, String node, Subject subject);

  /**
   * Delete a node permissions of a subject in a domain.
   * @param domain
   * @param node
   * @param subject
   * @param permission
   */
  void deleteSubjectPermissions(String domain, String node, Subject subject, String permission);

  /**
   * Add some node permissions for a subject in a domain.
   * @param domain
   * @param node
   * @param subject
   * @param permissions
   */
  void addSubjectPermissions(String domain, String node, Subject subject, Iterable<String> permissions);

  /**
   * A a node permission for a subject in a domain.
   * @param domain
   * @param node
   * @param subject
   * @param permission
   */
  void addSubjectPermission(String domain, String node, @NotNull Subject subject, @NotNull String permission);

  /**
   * Get all permissions of a subject.
   * @param subject
   * @return
   */
  Iterable<Permissions> getSubjectPermissions(Subject subject);

  /**
   * Get node permissions of a subject.
   * @param domain
   * @param node
   * @param subject
   * @return
   */
  Permissions getSubjectNodePermissions(@NotNull String domain, @NotNull String node, @NotNull Subject subject);

  /**
   * Get permissions of a subject on a node and its children.
   * @param domain
   * @param node
   * @param subject
   * @return
   */
  Iterable<Permissions> getSubjectNodeHierarchyPermissions(@NotNull String domain, @NotNull String node, @NotNull Subject subject);

  /**
   * Get the permissions for a node.
   * @param domain
   * @param node
   * @param type
   * @return
   */
  Iterable<Permissions> getNodePermissions(String domain, String node, @Nullable SubjectType type);

  /**
   * Get the permissions for a node and its children.
   * @param domain
   * @param node
   * @param type
   * @return
   */
  Iterable<Permissions> getNodeHierarchyPermissions(String domain, String node, @Nullable SubjectType type);

  /**
   * Get all subjects of a given type and having permissions in a domain.
   * @param domain
   * @param type
   * @return
   */
  Iterable<Subject> getSubjects(String domain, SubjectType type);

  class Subject implements Comparable<Subject> {

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
    public int compareTo(Subject o) {
      int diff = type.compareTo(o.type);
      if(diff == 0) {
        diff = getPrincipal().compareTo(o.getPrincipal());
      }
      return diff;
    }

    @Override
    public String toString() {
      return getType().toString() + ":" + getPrincipal();
    }

    @Override
    public boolean equals(Object obj) {
      if(this == obj) return true;
      if(obj == null) return false;
      if(obj instanceof Subject) {
        Subject rhs = (Subject) obj;
        return getPrincipal().equals(rhs.getPrincipal()) && getType() == rhs.getType();
      }
      return super.equals(obj);
    }

    @Override
    public int hashCode() {
      int h = 7;
      h = 31 * h + getPrincipal().hashCode();
      h = 31 * h + getType().hashCode();
      return h;
    }

  }

  enum SubjectType {
    USER, GROUP;

    public Subject subjectFor(String principal) {
      return new Subject(principal, this);
    }

  }

  interface Permissions {

    String getDomain();

    String getNode();

    Subject getSubject();

    Iterable<String> getPermissions();

  }

  interface SubjectAclChangeCallback {
    void onSubjectAclChanged(Subject subject);
  }
}
