package org.obiba.opal.spi.analysis;

import com.google.common.collect.Lists;
import org.json.JSONObject;
import org.obiba.magma.Variable;

import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Convenient class for implementing data processing engine specific analysis.
 *
 */
public class AbstractAnalysis implements Analysis {

  private static final SimpleDateFormat DATE_ID = new SimpleDateFormat("yyyyMMddHHmmssSSS");

  private final String id;

  private final String name;

  private final String templateName;

  private JSONObject parameters;

  private List<Variable> variables;

  public AbstractAnalysis(String name, String templateName) {
    this.id = DATE_ID.format(new Date()) + "-" + new Random().nextInt();
    this.name = name;
    this.templateName = templateName;
  }

  @Override
  public String getId() {
    return id;
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
  public List<Variable> getVariables() {
    return variables == null ? variables = Lists.newArrayList() : variables;
  }

  public void addVariables(List<Variable> variables) {
    if (variables == null) return;
    variables.forEach(this::addVariable);
  }

  public void addVariable(Variable var) {
    if (var == null || getVariables().contains(var)) return;
    getVariables().add(var);
  }
}
