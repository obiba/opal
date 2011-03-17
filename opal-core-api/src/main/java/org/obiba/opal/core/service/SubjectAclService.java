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

  void deleteSubjectPermissions(String domain, String node, Subject subject);

  void addSubjectPermissions(String domain, String node, Subject subject, Iterable<String> permissions);

  void addSubjectPermission(String domain, String node, Subject subject, String permission);

  Iterable<Permissions> getSubjectPermissions(Subject subject);

  Permissions getSubjectPermissions(String domain, String node, Subject subject);

  Iterable<Permissions> getNodePermissions(String domain, String node);

  Iterable<Subject> getSubjects(String domain);

  public class Subject implements Comparable<Subject> {

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
      int diff = type.compareTo(type);
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

  public enum SubjectType {
    USER, GROUP;

    public Subject subjectFor(String principal) {
      return new Subject(principal, this);
    }

  }

  public interface Permissions {

    String getDomain();

    String getNode();

    Subject getSubject();

    Iterable<String> getPermissions();

  }

  public interface SubjectAclChangeCallback {
    public void onSubjectAclChanged(Subject subject);
  }
}
