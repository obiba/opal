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

  void addListener(SubjectAclChangeCallback callback);

  void deleteNodePermissions(String domain, String node);

  void deleteSubjectPermissions(String domain, String node, String subject);

  void addSubjectPermissions(String domain, String node, String subject, Iterable<String> permissions);

  void addSubjectPermission(String domain, String node, String subject, String permission);

  Iterable<Permissions> getSubjectPermissions(String subject);

  Permissions getSubjectPermissions(String domain, String node, String subject);

  Iterable<Permissions> getNodePermissions(String domain, String node);

  Iterable<String> getSubjects(String domain);

  public interface Permissions {

    String getDomain();

    String getNode();

    String getSubject();

    Iterable<String> getPermissions();

  }

  public interface SubjectAclChangeCallback {
    public void onSubjectAclChanged(String subject);
  }
}
