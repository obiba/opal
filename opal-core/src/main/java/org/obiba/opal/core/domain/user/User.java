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

import org.obiba.core.util.HexUtil;
import org.obiba.opal.core.domain.AbstractOrientDbTimestampedEntity;

@SuppressWarnings("ComparableImplementedButEqualsNotOverridden")
public class User extends AbstractOrientDbTimestampedEntity implements Comparable<User> {

  public enum Status {
    ACTIVE, INACTIVE
  }

  @Nonnull
  private String name;

  @Nonnull
  private String password;

  private boolean enabled;

  private Set<Group> groups;

  public User() {
  }

  public User(@Nonnull String name) {
    this.name = name;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public void setName(@Nonnull String name) {
    this.name = name;
  }

  @Nonnull
  public String getPassword() {
    return password;
  }

  public void setPassword(@Nonnull String password) {
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

  public boolean hasGroup(Group group) {
    return groups.contains(group);
  }

  public void removeGroup(Group group) {
    if(getGroups().remove(group)) group.getUsers().remove(this);
  }

  public boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Digest the password into a predefined algorithm.
   *
   * @param password
   * @return
   */
  public static String digest(String password) {
    try {
      return HexUtil.bytesToHex(MessageDigest.getInstance("SHA").digest(password.getBytes()));
    } catch(NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int compareTo(User user) {
    return name.compareTo(user.name);
  }

}
