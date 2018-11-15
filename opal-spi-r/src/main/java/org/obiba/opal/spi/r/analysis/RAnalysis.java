package org.obiba.opal.spi.r.analysis;

import com.google.common.base.Strings;
import org.json.JSONObject;
import org.obiba.opal.spi.analysis.AbstractAnalysis;
import org.obiba.opal.spi.analysis.support.generator.IdGenetatorFactory;
import org.obiba.opal.spi.r.ROperationTemplate;

/**
 * R analysis are performed on tibble (see https://www.tidyverse.org/).
 */
public class RAnalysis extends AbstractAnalysis {

  private ROperationTemplate session;

  private String symbol;

  private RAnalysis(String name, String templateName) {
    super(name, templateName);
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

  public static Builder create(String name, String templateName) {
    return new Builder(name, templateName);
  }

  public static class Builder {

    private final RAnalysis analysis;

    private Builder(String name, String templateName) {
      this.analysis = new RAnalysis(name, templateName);
      this.analysis.setId(IdGenetatorFactory.createDateIdGenerator().generate());
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

    public RAnalysis build() {
      if (analysis.session == null) throw new IllegalArgumentException("No R session associated to the requested R analysis");
      if (Strings.isNullOrEmpty(analysis.symbol)) throw new IllegalArgumentException("Symbol to analyse is not defined");
      return analysis;
    }
  }

}
