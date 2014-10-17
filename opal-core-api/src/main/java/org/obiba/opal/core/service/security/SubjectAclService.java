/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service.security;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.SystemService;

public interface SubjectAclService extends SystemService {

  /**
   * Add permission change callback.
   *
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
   *
   * @param domain
   * @param node
   */
  void deleteNodeHierarchyPermissions(String domain, String node);

  /**
   * Delete all subject permissions.
   *
   * @param subject
   */
  void deleteSubjectPermissions(SubjectAcl.Subject subject);

  /**
   * Delete all node permissions of a subject in a domain.
   *
   * @param domain
   * @param node
   * @param subject
   */
  void deleteSubjectPermissions(String domain, String node, SubjectAcl.Subject subject);

  /**
   * Delete a node permissions of a subject in a domain.
   *
   * @param domain
   * @param node
   * @param subject
   * @param permission
   */
  void deleteSubjectPermissions(String domain, String node, SubjectAcl.Subject subject, String permission);

  /**
   * Add some node permissions for a subject in a domain.
   *
   * @param domain
   * @param node
   * @param subject
   * @param permissions
   */
  void addSubjectPermissions(String domain, String node, SubjectAcl.Subject subject, Iterable<String> permissions);

  /**
   * A a node permission for a subject in a domain.
   *
   * @param domain
   * @param node
   * @param subject
   * @param permission
   */
  void addSubjectPermission(String domain, String node, @NotNull SubjectAcl.Subject subject,
      @NotNull String permission);

  /**
   * Get all permissions of a subject.
   *
   * @param subject
   * @return
   */
  Iterable<Permissions> getSubjectPermissions(SubjectAcl.Subject subject);

  /**
   * Get node permissions of a subject.
   *
   * @param domain
   * @param node
   * @param subject
   * @return
   */
  Permissions getSubjectNodePermissions(@NotNull String domain, @NotNull String node,
      @NotNull SubjectAcl.Subject subject);

  /**
   * Get permissions of a subject on a node and its children.
   *
   * @param domain
   * @param node
   * @param subject
   * @return
   */
  Iterable<Permissions> getSubjectNodeHierarchyPermissions(@NotNull String domain, @NotNull String node,
      @NotNull SubjectAcl.Subject subject);

  /**
   * Get the permissions for a node.
   *
   * @param domain
   * @param node
   * @param type
   * @return
   */
  Iterable<Permissions> getNodePermissions(String domain, String node, @Nullable SubjectAcl.SubjectType type);

  /**
   * Get the permissions for a node and its children.
   *
   * @param domain
   * @param node
   * @param type
   * @return
   */
  Iterable<Permissions> getNodeHierarchyPermissions(String domain, String node, @Nullable SubjectAcl.SubjectType type);

  /**
   * Get all subjects of a given type and having permissions in a domain.
   *
   * @param domain
   * @param type
   * @return
   */
  Iterable<SubjectAcl.Subject> getSubjects(String domain, SubjectAcl.SubjectType type);

  interface Permissions {

    String getDomain();

    String getNode();

    SubjectAcl.Subject getSubject();

    Iterable<String> getPermissions();

  }

  interface SubjectAclChangeCallback {
    void onSubjectAclChanged(SubjectAcl.Subject subject);
  }
}
