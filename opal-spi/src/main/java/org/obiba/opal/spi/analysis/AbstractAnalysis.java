package org.obiba.opal.spi.analysis;

import com.google.common.collect.Lists;
import org.json.JSONObject;
import org.obiba.opal.spi.analysis.support.generator.IdGenetatorFactory;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Convenient class for implementing data processing engine specific analysis.
 *
 */
public abstract class AbstractAnalysis implements Analysis {

  private String id;

  private final String name;

  private final String pluginName;

  private final String templateName;

  private JSONObject parameters;

  private List<String> variables;

  public AbstractAnalysis(@NotNull String name, @NotNull String pluginName,
      @NotNull String templateName) {

    Assert.notNull(name, "name cannot be null");
    Assert.notNull(templateName , "templateName cannot be null");
    Assert.notNull(templateName , "pluginName cannot be null");

    this.id = IdGenetatorFactory.createDateIdGenerator().generate();
    this.name = name;
    this.pluginName = pluginName;
    this.templateName = templateName;
  }

  @NotNull
  @Override
  public String getId() {
    return id;
  }

  @NotNull
  @Override
  public String getName() {
    return name;
  }

  @NotNull
  @Override
  public String getTemplateName() {
    return templateName;
  }

  @Override
  public JSONObject getParameters() {
    return parameters;
  }

  protected void setParameters(JSONObject parameters) {
    this.parameters = parameters;
  }

  @Override
  public String getPluginName() {
    return pluginName;
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
