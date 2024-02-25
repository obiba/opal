/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.domain.security;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.obiba.opal.core.domain.AbstractTimestamped;
import org.obiba.opal.core.domain.HasUniqueProperties;

import com.google.common.collect.Lists;

public class Group extends AbstractTimestamped implements HasUniqueProperties, Comparable<Group> {

  @NotNull
  @NotBlank
  private String name;

  private Set<String> subjectCredentials = new HashSet<>();

  public Group() {
  }

  public Group(@NotNull String name) {
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

  public Set<String> getSubjectCredentials() {
    return subjectCredentials;
  }

  public void setSubjectCredentials(Set<String> subjectCredentials) {
    this.subjectCredentials = subjectCredentials;
  }

  public void addSubjectCredential(String subjectName) {
    if(subjectCredentials == null) subjectCredentials = new HashSet<>();
    subjectCredentials.add(subjectName);
  }

  public void removeSubjectCredential(String subjectName) {
    if(subjectCredentials != null) subjectCredentials.remove(subjectName);
  }

  public boolean hasSubjectCredential(String subjectName) {
    return subjectCredentials != null && subjectCredentials.contains(subjectName);
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

    public Builder subjectCredentials(Set<String> subjectCredentials) {
      group.subjectCredentials = subjectCredentials;
      return this;
    }

    public Group build() {
      return group;
    }
  }
}
