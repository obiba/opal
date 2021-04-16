/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.analysis;

import com.google.common.base.Strings;
import org.json.JSONObject;
import org.obiba.opal.spi.analysis.AbstractAnalysis;
import org.obiba.opal.spi.r.ROperationTemplate;

import java.util.List;

/**
 * R analysis are performed on tibble (see https://www.tidyverse.org/).
 */
public class RAnalysis extends AbstractAnalysis {

  private ROperationTemplate session;

  private String symbol;

  private RAnalysis(String datasource, String table, String name, String pluginName, String templateName) {
    super(datasource, table, name, pluginName, templateName);
  }

  /**
   * R operation executor in the R session.
   *
   * @return
   */
  public ROperationTemplate getSession() {
    return session;
  }

  /**
   * The tibble symbol in the R session on which analysis will be run.
   *
   * @return
   */
  public String getSymbol() {
    return symbol;
  }

  public static Builder create(String datasource, String table, String name, String pluginName, String templateName) {
    return new Builder(datasource, table, name, pluginName, templateName);
  }

  public static class Builder {

    private final RAnalysis analysis;

    private Builder(String datasource, String table, String name, String pluginName, String templateName) {
      this.analysis = new RAnalysis(datasource, table, name, pluginName, templateName);
    }

    public Builder session(ROperationTemplate session) {
      analysis.session = session;
      return this;
    }

    public Builder symbol(String symbol) {
      analysis.symbol = symbol;
      return this;
    }

    public Builder parameters(JSONObject parameters) {
      analysis.setParameters(parameters);
      return this;
    }

    public Builder variables(List<String> value) {
      analysis.setVariables(value);
      return this;
    }

    public RAnalysis build() {
      if (analysis.session == null) throw new IllegalArgumentException("No R session associated to the requested R analysis");
      if (Strings.isNullOrEmpty(analysis.symbol)) throw new IllegalArgumentException("Symbol to analyse is not defined");
      return analysis;
    }
  }

}
