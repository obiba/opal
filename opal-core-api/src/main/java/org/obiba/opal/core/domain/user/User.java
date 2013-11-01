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
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.obiba.core.util.HexUtil;
import org.obiba.opal.core.domain.AbstractTimestamped;
import org.obiba.opal.core.domain.HasUniqueProperties;

import com.google.common.collect.Lists;

public class User extends AbstractTimestamped implements Comparable<User>, HasUniqueProperties {

  public enum Status {
    ACTIVE, INACTIVE
  }

  @NotNull
  @NotBlank
  private String name;

  @NotNull
  @NotBlank
  private String password;

  private boolean enabled;

  private Set<String> groups = new HashSet<String>();

  public User() {
  }

  public User(@NotNull String name) {
    this.name = name;
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("name");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.<Object>newArrayList(name);
  }

  @NotNull
  public String getName() {
    return name;
  }

  public void setName(@NotNull String name) {
    this.name = name;
  }

  @NotNull
  public String getPassword() {
    return password;
  }

  public void setPassword(@NotNull String password) {
    this.password = password;
  }

  public Set<String> getGroups() {
    return groups;
  }

  public void setGroups(Set<String> groups) {
    this.groups = groups;
  }

  public void addGroup(String group) {
    if(groups == null) groups = new HashSet<String>();
    groups.add(group);
  }

  public boolean hasGroup(String group) {
    return groups != null && groups.contains(group);
  }

  public void removeGroup(String group) {
    if(groups != null) groups.remove(group);
  }

  public boolean isEnabled() {
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
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(!(o instanceof User)) return false;
    User user = (User) o;
    return name.equals(user.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public int compareTo(User user) {
    return name.compareTo(user.name);
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private User user;

    private Builder() {
    }

    public static Builder create() {
      Builder builder = new Builder();
      builder.user = new User();
      return builder;
    }

    public Builder name(String name) {
      user.name = name;
      return this;
    }

    public Builder password(String password) {
      user.password = password;
      return this;
    }

    public Builder enabled(boolean enabled) {
      user.enabled = enabled;
      return this;
    }

    public Builder groups(Set<String> groups) {
      user.groups = groups;
      return this;
    }

    public Builder group(String group) {
      user.addGroup(group);
      return this;
    }

    public User build() {
      return user;
    }
  }

}
