/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.user;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.obiba.core.domain.AbstractEntity;

@SuppressWarnings("UnusedDeclaration")
@Entity
@Table(name = "group")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Group extends AbstractEntity implements Comparable<Group> {

  private static final long serialVersionUID = -5985745491689725964L;

  @Column(nullable = false, unique = true)
  private String name = null;

  @ManyToMany
  @JoinTable(name = "group_users", joinColumns = { @JoinColumn(name = "group_id") }, inverseJoinColumns = @JoinColumn(
      name = "user_id"))
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
  private Set<User> users;

  public Group() {
  }

  public Group(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<User> getUsers() {
    return users != null ? users : (users = new HashSet<User>());
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public int compareTo(Group o) {
    return name.compareTo(o.name);
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof Group) {
      Group o = (Group) obj;
      return name.equals(o.name);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

}
