/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.domain.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.obiba.core.util.HexUtil;
import org.obiba.magma.datasource.hibernate.domain.AbstractTimestampedEntity;

@SuppressWarnings("UnusedDeclaration")
@Entity
@Table(name = "user")
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class User extends AbstractTimestampedEntity {

  private static final long serialVersionUID = -2200053643926715563L;

  @Nonnull
  @Column(length = 250, nullable = false, unique = true)
  private String name;

  @Column(length = 250)
  private String password;

  private boolean enabled;

  @ManyToMany
  @JoinTable(name = "user_groups", joinColumns = { @JoinColumn(name = "user_id") }, inverseJoinColumns = @JoinColumn(
      name = "group_id"))
  private Set<Group> groups;

  public User() {
  }

  public User(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Set<Group> getGroups() {
    return groups == null ? (groups = new HashSet<Group>()) : groups;
  }

  public void setGroups(Set<Group> groups) {
    this.groups = groups;
  }

  public void addGroup(Group group) {
    if(!getGroups().contains(group)) {
      getGroups().add(group);
    }
  }

  public boolean hasGroup(Group g) {
    return groups.contains(g);
  }

  public void removeGroup(Group g) {
    if(getGroups().remove(g)) g.getUsers().remove(this);
  }

  public boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(boolean b) {
    enabled = b;
  }

  /**
   * Digest the password into a predefined algorithm.
   *
   * @param password
   * @return
   */
  @SuppressWarnings("CallToPrintStackTrace")
  public static String digest(String password) {
    try {
      return HexUtil.bytesToHex(MessageDigest.getInstance("SHA").digest(password.getBytes()));
    } catch(NoSuchAlgorithmException e) {
      e.printStackTrace();
      return password;
    }
  }

//  public boolean isActive() {
//    return getStatus() == ACTIVE;
//  }
}
