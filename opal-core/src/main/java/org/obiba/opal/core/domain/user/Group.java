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

import org.obiba.opal.core.domain.AbstractOrientDbTimestampedEntity;

@SuppressWarnings("ComparableImplementedButEqualsNotOverridden")
public class Group extends AbstractOrientDbTimestampedEntity implements Comparable<Group> {

  @Nonnull
  private String name;

  private Set<User> users;

  public Group() {
  }

  public Group(@Nonnull String name) {
    this.name = name;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public void setName(@Nonnull String name) {
    this.name = name;
  }

  public Set<User> getUsers() {
    return users == null ? (users = new HashSet<User>()) : users;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public int compareTo(Group group) {
    return name.compareTo(group.name);
  }

}
