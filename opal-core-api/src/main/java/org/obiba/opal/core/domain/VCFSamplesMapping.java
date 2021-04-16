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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import javax.validation.constraints.NotNull;
import java.util.List;

public class VCFSamplesMapping implements HasUniqueProperties {

  @NotNull
  private String projectName;

  @NotNull
  private String tableReference;

  @NotNull
  private String participantIdVariable;

  @NotNull
  private String sampleRoleVariable;

  public VCFSamplesMapping() {
  }

  public VCFSamplesMapping(String name) {
    projectName = name;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(VCFSamplesMapping vcfSamplesMapping) {
    return new Builder(vcfSamplesMapping);
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public boolean hasTableReference() {
    return !Strings.isNullOrEmpty(tableReference);
  }

  public String getTableReference() {
    return tableReference;
  }

  public void setTableReference(String tableReference) {
    this.tableReference = tableReference;
  }

  public String getParticipantIdVariable() {
    return participantIdVariable;
  }

  public void setParticipantIdVariable(String participantIdVariable) {
    this.participantIdVariable = participantIdVariable;
  }

  public String getSampleRoleVariable() {
    return sampleRoleVariable;
  }

  public void setSampleRoleVariable(String sampleRoleVariable) {
    this.sampleRoleVariable = sampleRoleVariable;
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("projectName");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.<Object>newArrayList(projectName);
  }

  public static class Builder {
    private VCFSamplesMapping vcfSamplesMapping;

    private Builder() {
      vcfSamplesMapping = new VCFSamplesMapping();
    }

    public Builder(VCFSamplesMapping samplesMapping) {
      vcfSamplesMapping = new VCFSamplesMapping();
      vcfSamplesMapping.projectName = samplesMapping.projectName;
      vcfSamplesMapping.tableReference = samplesMapping.tableReference;
      vcfSamplesMapping.participantIdVariable = samplesMapping.participantIdVariable;
      vcfSamplesMapping.sampleRoleVariable = samplesMapping.sampleRoleVariable;
    }

    public Builder projectName(String value) {
      vcfSamplesMapping.projectName = value;
      return this;
    }

    public Builder tableName(String value) {
      vcfSamplesMapping.tableReference = value;
      return this;
    }

    public Builder participantIdVariable(String value) {
      vcfSamplesMapping.participantIdVariable = value;
      return this;
    }

    public Builder sampleRoleVariable(String value) {
      vcfSamplesMapping.sampleRoleVariable = value;
      return this;
    }

    public VCFSamplesMapping build() {
      return vcfSamplesMapping;
    }
  }
}
