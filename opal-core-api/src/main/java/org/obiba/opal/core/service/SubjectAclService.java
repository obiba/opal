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

public interface SubjectAclService {

  void addSubjectPermissions(String node, String subject, Iterable<String> permissions);

  void addSubjectPermission(String node, String subject, String permission);

  Iterable<SubjectPermission> getSubjectPermissions(String subject);

  Iterable<NodePermission> getNodePermissions(String node);

  Iterable<String> getSubjectPermissions(String node, String subject);

  public interface NodePermission {

    String getSubject();

    Iterable<String> getPermissions();

  }

  public interface SubjectPermission {

    String getNode();

    Iterable<String> getPermissions();

  }
}
