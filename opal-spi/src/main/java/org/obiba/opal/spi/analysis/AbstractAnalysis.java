package org.obiba.opal.spi.analysis;

import com.google.common.collect.Lists;
import org.json.JSONObject;
import org.obiba.magma.Variable;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Convenient class for implementing data processing engine specific analysis.
 *
 */
public abstract class AbstractAnalysis implements Analysis {


  private String id;

  private final String name;

  private final String templateName;

  private JSONObject parameters;

  private List<String> variables;

  public AbstractAnalysis(String name, String templateName) {
    this.name = name;
    this.templateName = templateName;
  }

  @Override
  public String getId() {
    return id;
  }

  protected void setId(String value) {
    id = value;
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

  protected void setParameters(JSONObject parameters) {
    this.parameters = parameters;
  }

  @Override
  public List<String> getVariables() {
    return variables == null ? variables = Lists.newArrayList() : variables;
  }

  protected void setVariables(List<String> variables) {
    if (variables == null) return;
    this.variables = variables;
  }

}
