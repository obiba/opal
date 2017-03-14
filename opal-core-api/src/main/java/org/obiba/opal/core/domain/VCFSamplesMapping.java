package org.obiba.opal.core.domain;

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
  private String sampleIdVariable;

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

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
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

  public String getSampleIdVariable() {
    return sampleIdVariable;
  }

  public void setSampleIdVariable(String sampleIdVariable) {
    this.sampleIdVariable = sampleIdVariable;
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

    public Builder sampleIdVariable(String value) {
      vcfSamplesMapping.sampleIdVariable = value;
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
