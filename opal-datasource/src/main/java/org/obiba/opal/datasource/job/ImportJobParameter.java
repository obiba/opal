package org.obiba.opal.datasource.job;

import org.springframework.batch.core.JobParameter;

public class ImportJobParameter {

  private String name;

  private JobParameter.ParameterType type;

  private String description;

  public ImportJobParameter(String name, JobParameter.ParameterType type, String description) {
    this.name = name;
    this.type = type;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public JobParameter.ParameterType getType() {
    return type;
  }

  public String getDescription() {
    return description;
  }
}
