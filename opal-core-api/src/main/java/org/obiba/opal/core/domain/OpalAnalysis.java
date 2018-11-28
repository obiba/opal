package org.obiba.opal.core.domain;

import com.google.common.collect.Lists;
import org.json.JSONObject;
import org.obiba.opal.spi.analysis.Analysis;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public class OpalAnalysis extends AbstractTimestamped implements Analysis, HasUniqueProperties {

  private static final String DEFAULT_ID = "empty";

  private String id;
  private String name;
  private String templateName;
  private String pluginName;
  private JSONObject parameters;
  private List<String> variables;

  private String datasource;
  private String table;

  public OpalAnalysis() {
    this.id = DEFAULT_ID;
  }

  public OpalAnalysis(@NotNull String datasource, @NotNull String table, @NotNull Analysis analysis) {
    Assert.notNull(datasource, "datasource cannot be null");
    Assert.notNull(table , "table cannot be null");
    Assert.notNull(analysis, "analysis cannot be null");

    id = analysis.getId();
    name = analysis.getName();
    templateName = analysis.getTemplateName();
    parameters = Optional.of(analysis.getParameters()).orElseGet(JSONObject::new);
    variables = Optional.of(analysis.getVariables()).orElseGet(Lists::newArrayList);
    pluginName = analysis.getPluginName();

    this.datasource = datasource;
    this.table = table;
  }

  public String getDatasource() {
    return datasource;
  }

  public void setDatasource(String datasource) {
    this.datasource = datasource;
  }

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
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

  @Override
  public String getPluginName() {
    return pluginName;
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

  @Override
  public List<String> getVariables() {
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
