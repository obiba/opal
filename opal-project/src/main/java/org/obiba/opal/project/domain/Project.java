/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.project.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Description of a project in Opal.
 */
public class Project {

  private String name;

  private String description;

  private List<String> tags;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean hasDescription() {
    return description != null;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean hasTags() {
    return getTags().size()>0;
  }

  public List<String> getTags() {
    return tags == null ? tags = new ArrayList<String>() : tags;
  }

  public void addTag(String tag) {
    if(!getTags().contains(tag)) {
      getTags().add(tag);
    }
  }

  public static class Builder {

    private Project project;

    public static Builder create(String name) {
      Builder builder = new Builder();
      builder.project = new Project();
      builder.project.setName(name);
      return builder;
    }

    public Builder name(String name) {
      project.setName(name);
      return this;
    }

    public Builder description(String description) {
      project.setDescription(description);
      return this;
    }

    public Builder tags(List<String> tags) {
      if (tags == null) return this;
      for(String tag : tags) {
        project.addTag(tag);
      }
      return this;
    }

    public Builder tags(String... tags) {
      if (tags == null) return this;
      for(String tag : tags) {
        project.addTag(tag);
      }
      return this;
    }

    public Project build() {
      return project;
    }
  }
}
