/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.domain;

import java.beans.Transient;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.google.common.collect.Sets;
import org.obiba.magma.*;
import org.obiba.magma.datasource.nil.NullDatasource;
import org.obiba.magma.type.DateTimeType;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Description of a project in Opal.
 */
public class Project extends AbstractTimestamped implements HasUniqueProperties, Comparable<Project>, org.obiba.magma.Timestamped {

  @NotNull
  @NotBlank
  private String name;

  @NotNull
  @NotBlank
  private String title;

  private String description;

  private Set<String> tags;

  private boolean archived;

  private String database;

  private String vcfStoreService;

  private String exportFolder;

  private Set<ProjectIdentifiersMapping> identifiersMappings;

  public Project() {
  }

  public Project(@NotNull String name) {
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
  public String getTitle() {
    return title;
  }

  public void setTitle(@NotNull String title) {
    this.title = title;
  }

  public boolean hasDescription() {
    return !Strings.isNullOrEmpty(description);
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean hasTags() {
    return tags != null && !tags.isEmpty();
  }

  public Set<String> getTags() {
    return tags;
  }

  public void addTag(String tag) {
    if(tags == null) tags = new HashSet<>();
    tags.add(tag);
  }

  public boolean isArchived() {
    return archived;
  }

  public void setArchived(boolean archived) {
    this.archived = archived;
  }

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public boolean hasDatabase() {
    return !Strings.isNullOrEmpty(database);
  }

  public boolean hasVCFStoreService() {
    return !Strings.isNullOrEmpty(vcfStoreService);
  }

  public String getVCFStoreService() {
    return vcfStoreService;
  }

  public void setVCFStoreService(String vcfStore) {
    this.vcfStoreService = vcfStore;
  }

  public boolean hasExportFolder() {
    return !Strings.isNullOrEmpty(exportFolder);
  }

  public String getExportFolder() {
    return exportFolder;
  }

  void setExportFolder(String exportFolder) {
    this.exportFolder = exportFolder;
  }

  @Transient
  public Datasource getDatasource() {
    if (MagmaEngine.get().hasDatasource(name))
      return MagmaEngine.get().getDatasource(name);
    // if datasource init failed or permission is denied, return the null one so that project keeps being accessible
    return new NullDatasource(name);
  }

  @Transient
  public boolean hasDatasource() {
    return MagmaEngine.get().hasDatasource(name);
  }

  public boolean hasIdentifiersMappings() {
    return identifiersMappings != null && identifiersMappings.size() > 0;
  }

  public Set<ProjectIdentifiersMapping> getIdentifiersMappings() {
    return identifiersMappings;
  }

  public void removeIdentifiersMappingByEntityType(String entityType) {
    if (hasIdentifiersMappings()) {
      getIdentifiersMappings().removeIf(mapping -> mapping.getEntityType().equals(entityType));
    }
  }

  public void removeIdentifiersMappingByMapping(String entityType, String name) {
    if (hasIdentifiersMappings()) {
      getIdentifiersMappings()
        .removeIf(mapping -> mapping.getEntityType().equals(entityType) && mapping.getMapping().equals(name));
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("name", name).add("database", database).toString();
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(!(o instanceof Project)) return false;
    Project project = (Project) o;
    return name.equals(project.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public int compareTo(Project project) {
    return name.compareTo(project.name);
  }

  @Nonnull
  @Override
  public Timestamps getTimestamps() {
    return new Timestamps() {
      @Nonnull
      @Override
      public Value getLastUpdate() {
        return DateTimeType.get().valueOf(getUpdated());
      }

      @Nonnull
      @Override
      public Value getCreated() {
        return DateTimeType.get().valueOf(Project.this.getCreated());
      }
    };
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private Project project;

    private Builder() {
    }

    public static Builder create(@Nullable Project project) {
      Builder builder = new Builder();
      builder.project = project == null ? new Project() : project;
      return builder;
    }

    public static Builder create() {
      return create(null);
    }

    public Builder name(String name) {
      project.setName(name);
      return this;
    }

    public Builder title(String title) {
      project.setTitle(title);
      return this;
    }

    public Builder description(String description) {
      project.setDescription(description);
      return this;
    }

    public Builder tags(Iterable<String> tags) {
      if(tags == null) return this;
      for(String tag : tags) {
        project.addTag(tag);
      }
      return this;
    }

    public Builder tags(String... tags) {
      if(tags == null) return this;
      for(String tag : tags) {
        project.addTag(tag);
      }
      return this;
    }

    public Builder archived(boolean archived) {
      project.setArchived(archived);
      return this;
    }

    public Builder archived() {
      return archived(true);
    }

    public Builder database(String database) {
      project.setDatabase(database);
      return this;
    }

    public Builder vcfStoreService(String service) {
      project.setVCFStoreService(service);
      return this;
    }

    public Builder exportFolder(String value) {
      project.setExportFolder(value);
      return this;
    }

    public Builder idMapping(ProjectIdentifiersMapping value) {
      if (project.identifiersMappings == null) {
        project.identifiersMappings = Sets.newHashSet();
      }

      project.identifiersMappings.add(value);
      return this;
    }

    public Project build() {
      return project;
    }
  }
}
