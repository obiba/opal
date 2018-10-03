package org.obiba.opal.spi.r.analysis;

import org.json.JSONObject;
import org.obiba.opal.spi.analysis.AnalysisAdapter;
import org.obiba.opal.spi.r.ROperationTemplate;

/**
 * R analysis are performed on tibble (see https://www.tidyverse.org/).
 */
public class RAnalysis extends AnalysisAdapter {

  private final ROperationTemplate session;

  private final String symbol;

  public RAnalysis(String name, String templateName, JSONObject parameters, ROperationTemplate session, String symbol) {
    super(name, templateName, parameters);
    this.session = session;
    this.symbol = symbol;
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

}
