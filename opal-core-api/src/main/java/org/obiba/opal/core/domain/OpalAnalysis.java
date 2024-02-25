/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import java.beans.Transient;
import java.util.List;
import jakarta.annotation.Nullable;
import javax.validation.constraints.NotNull;
import org.json.JSONObject;
import org.obiba.opal.spi.analysis.Analysis;
import org.springframework.util.Assert;

public class OpalAnalysis extends AbstractTimestamped implements Analysis, HasUniqueProperties {

  private String name;
  private String templateName;
  private String pluginName;
  private String parametersString;
  private List<String> variables;

  private String datasource;
  private String table;

  @Override
  public String getDatasource() {
    return datasource;
  }

  @Override
  public String getTable() {
    return table;
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

  /**
   * OrientDB does not serialize nicely a JSONArray, it adds a field 'myArrayList' that breaks the analysis process.
   * Saving as string resolves the issue.
   *
   * @return
   */
  public String getParametersAsString() {
    return parametersString;
  }

  @JsonIgnore
  @Override
  @Transient
  public JSONObject getParameters() {
    return new JSONObject(getParametersAsString());
  }

  @Override
  public List<String> getVariables() {
    return variables;
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("name", "datasource", "table");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.newArrayList(name, datasource, table);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("datasource", datasource)
        .add("table", table)
        .add("parameters", parametersString)
        .add("variables", variables)
        .toString();
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
        .name(analysis.getName())
        .datasource(datasource)
        .table(table)
        .pluginName(analysis.getPluginName())
        .templateName(analysis.getTemplateName())
        .parameters(analysis.getParameters())
        .variables(analysis.getVariables());
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
      analysis.parametersString = value == null ? "{}" : value.toString();
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
