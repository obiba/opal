package org.obiba.opal.spi.analysis;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Convenient class for implementing data processing engine specific analysis.
 *
 */
public class AnalysisAdapter implements Analysis {

  private final String name;

  private final String templateName;

  private JSONObject parameters;

  private List<String> variables;

  public AnalysisAdapter(String name, String templateName) {
    this.name = name;
    this.templateName = templateName;
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

  public void setParameters(JSONObject parameters) {
    this.parameters = parameters;
  }

  @Override
  public List<String> getVariables() {
    return variables == null ? variables = Lists.newArrayList() : variables;
  }

  public void addVariables(List<String> variables) {
    if (variables == null) return;
    variables.forEach(this::addVariable);
  }

  public void addVariable(String var) {
    if (Strings.isNullOrEmpty(var) || getVariables().contains(var)) return;
    getVariables().add(var);
  }
}
