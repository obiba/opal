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

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.hibernate.validator.constraints.NotBlank;
import org.obiba.opal.core.domain.AbstractTimestamped;

public class Group extends AbstractTimestamped implements Comparable<Group> {

  @Nonnull
  @NotBlank
  private String name;

  private Set<String> users = new HashSet<String>();

  @Nonnull
  public String getName() {
    return name;
  }

  public void setName(@Nonnull String name) {
    this.name = name;
  }

  public Set<String> getUsers() {
    return users;
  }

  public void setUsers(Set<String> users) {
    this.users = users;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(!(o instanceof Group)) return false;
    Group group = (Group) o;
    return name.equals(group.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public int compareTo(Group group) {
    return name.compareTo(group.name);
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private Group group;

    private Builder() {
    }

    public static Builder create() {
      Builder builder = new Builder();
      builder.group = new Group();
      return builder;
    }

    public Builder name(String name) {
      group.name = name;
      return this;
    }

    public Builder users(Set<String> users) {
      group.users = users;
      return this;
    }

    public Group build() {
      return group;
    }
  }
}
