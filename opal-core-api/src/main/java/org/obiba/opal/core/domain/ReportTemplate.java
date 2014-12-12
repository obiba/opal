/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

public class ReportTemplate extends AbstractTimestamped implements HasUniqueProperties, Comparable<ReportTemplate> {

  @NotNull
  @NotBlank
  private String name;

  @NotNull
  @NotBlank
  private String project;

  private String design;

  private String format;

  @NotNull
  private final Map<String, String> parameters = new HashMap<>();

  private String schedule;

  @NotNull
  private final Set<String> emailNotificationAddresses = new HashSet<>();

  public ReportTemplate() {
  }

  public ReportTemplate(@NotNull String name, @NotNull String project) {
    this.name = name;
    this.project = project;
  }

  @NotNull
  public String getName() {
    return name;
  }

  public void setName(@NotNull String name) {
    this.name = name;
  }

  @NotNull
  public String getProject() {
    return project;
  }

  public void setProject(@NotNull String project) {
    this.project = project;
  }

  public String getDesign() {
    return design;
  }

  public void setDesign(String design) {
    this.design = design;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  @NotNull
  public Map<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(@Nullable Map<String, String> parameters) {
    this.parameters.clear();
    if(parameters != null) {
      this.parameters.putAll(parameters);
    }
  }

  public String getSchedule() {
    return schedule;
  }

  public void setSchedule(String schedule) {
    this.schedule = schedule;
  }

  public boolean hasSchedule() {
    return schedule != null;
  }

  @NotNull
  public Set<String> getEmailNotificationAddresses() {
    return emailNotificationAddresses;
  }

  public void setEmailNotificationAddresses(@Nullable Collection<String> emailNotificationAddresses) {
    this.emailNotificationAddresses.clear();
    if(emailNotificationAddresses != null) {
      this.emailNotificationAddresses.addAll(emailNotificationAddresses);
    }
  }

  @Override
  public int compareTo(@NotNull ReportTemplate other) {
    return ComparisonChain.start().compare(project, other.project).compare(name, other.name).result();
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("name", "project");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.<Object>newArrayList(name, project);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, project);
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj == null || getClass() != obj.getClass()) return false;
    ReportTemplate other = (ReportTemplate) obj;
    return Objects.equals(name, other.name) && Objects.equals(project, other.project);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("name", name).add("project", project).toString();
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private ReportTemplate reportTemplate;

    private Builder() {
    }

    public static Builder create(@Nullable ReportTemplate reportTemplate) {
      Builder builder = new Builder();
      builder.reportTemplate = reportTemplate == null ? new ReportTemplate() : reportTemplate;
      return builder;
    }

    public static Builder create() {
      return create(null);
    }

    public Builder nameAndProject(String name, String project) {
      reportTemplate.setName(name);
      reportTemplate.setProject(project);
      return this;
    }

    public Builder design(String design) {
      reportTemplate.setDesign(design);
      return this;
    }

    public Builder project(String project) {
      reportTemplate.setProject(project);
      return this;
    }

    public Builder format(String format) {
      reportTemplate.setFormat(format);
      return this;
    }

    public Builder schedule(String schedule) {
      reportTemplate.setSchedule(schedule);
      return this;
    }

    public Builder parameter(String name, String value) {
      reportTemplate.getParameters().put(name, value);
      return this;
    }

    public Builder emailNotificationAddresses(Collection<String> emails) {
      reportTemplate.setEmailNotificationAddresses(emails);
      return this;
    }

    public Builder emailNotificationAddress(String email) {
      reportTemplate.getEmailNotificationAddresses().add(email);
      return this;
    }

    public ReportTemplate build() {
      return reportTemplate;
    }
  }
}
