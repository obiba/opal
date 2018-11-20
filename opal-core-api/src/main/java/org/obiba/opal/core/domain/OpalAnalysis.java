package org.obiba.opal.core.domain;

import com.google.common.collect.Lists;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.json.JSONObject;
import org.obiba.magma.Variable;
import org.obiba.opal.spi.analysis.Analysis;

public class OpalAnalysis implements Analysis, HasUniqueProperties {

  private static final String DEFAULT_ID = "empty";

  private String id;
  private String name;
  private String templateName;
  private JSONObject parameters;
  private List<Variable> variables;

  public OpalAnalysis() {
    this.id = DEFAULT_ID;
  }

  public OpalAnalysis(@NotNull Analysis analysis) {
    id = analysis.getId();
    name = analysis.getName();
    templateName = analysis.getTemplateName();
    parameters = analysis.getParameters();
    variables = analysis.getVariables();
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

  @Override
  public JSONObject getParameters() {
    return parameters;
  }

  @Override
  public List<Variable> getVariables() {
    return variables;
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("id");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.newArrayList(id);
  }
}
