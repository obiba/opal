package org.obiba.opal.spi.analysis;

import org.json.JSONObject;

import javax.validation.constraints.NotNull;

/**
 * Convenient class for implementing data processing engine specific analysis.
 *
 */
public class AnalysisAdapter implements Analysis {

  private final String name;

  private final String templateName;

  private final JSONObject parameters;

  public AnalysisAdapter(String name, String templateName, JSONObject parameters) {
    this.name = name;
    this.templateName = templateName;
    this.parameters = parameters;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getTemplateName() {
    return templateName;
  }

  @NotNull
  @Override
  public JSONObject getParameters() {
    return parameters;
  }
}
