package org.obiba.opal.core.domain;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.json.JSONObject;
import org.obiba.opal.spi.analysis.Analysis;
import org.obiba.opal.spi.analysis.support.generator.IdGenetatorFactory;
import org.springframework.util.Assert;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

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

  public String getDatasource() {
    return datasource;
  }

  public String getTable() {
    return table;
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

  public static class Builder {
    private OpalAnalysis analysis;

    private Builder() {}

    public static Builder create(@Nullable OpalAnalysis opalAnalysis) {
      Builder builder = new Builder();
      builder.analysis = opalAnalysis == null ? new OpalAnalysis() : opalAnalysis;
      return builder;
    }

    public static Builder create(@NotNull String datasource, @NotNull String table, @NotNull Analysis analysis) {
      Assert.notNull(datasource, "datasource cannot be null");
      Assert.notNull(table , "table cannot be null");
      Assert.notNull(analysis, "analysis cannot be null");

      if (analysis instanceof OpalAnalysis) {
        return create((OpalAnalysis)analysis).datasource(datasource).table(table);
      }

      return create(null)
        .id(analysis.getId())
        .name(analysis.getName())
        .datasource(datasource)
        .table(table)
        .pluginName(analysis.getPluginName())
        .templateName(analysis.getTemplateName())
        .parameters(analysis.getParameters())
        .variables(analysis.getVariables());
    }

    Builder id(String value) {
      analysis.id = Strings.isNullOrEmpty(value) ? IdGenetatorFactory.createDateIdGenerator().generate() : value;
      return this;
    }

    public Builder name(String value) {
      analysis.name = value;
      return this;
    }

    public Builder datasource(String value) {
      analysis.datasource = value;
      return this;
    }

    public Builder table(String value) {
      analysis.table = value;
      return this;
    }

    public Builder pluginName(String value) {
      analysis.pluginName = value;
      return this;
    }

    public Builder templateName(String value) {
      analysis.templateName= value;
      return this;
    }

    public Builder parameters(JSONObject value) {
      analysis.parameters = value;
      return this;
    }

    public Builder variables(List<String> value) {
      analysis.variables = value;
      return this;
    }

    public OpalAnalysis build() {
      return analysis;
    }

  }

}
