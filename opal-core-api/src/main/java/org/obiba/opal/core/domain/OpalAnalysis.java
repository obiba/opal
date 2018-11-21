package org.obiba.opal.core.domain;

import com.google.common.collect.Lists;
import org.json.JSONObject;
import org.obiba.opal.spi.analysis.Analysis;

import javax.validation.constraints.NotNull;
import java.util.List;

public class OpalAnalysis implements Analysis, HasUniqueProperties {

  private static final String DEFAULT_ID = "empty";

  private String id;
  private String name;
  private String templateName;
  private JSONObject parameters;
  private List<String> variables;

  private String datasource;
  private String table;

  public OpalAnalysis() {
    this.id = DEFAULT_ID;
  }

  public OpalAnalysis(@NotNull String datasource, @NotNull String table, @NotNull Analysis analysis) {
    id = analysis.getId();
    name = analysis.getName();
    templateName = analysis.getTemplateName();
    parameters = analysis.getParameters();
    variables = analysis.getVariables();

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
