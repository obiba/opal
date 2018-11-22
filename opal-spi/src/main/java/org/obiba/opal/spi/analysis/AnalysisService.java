package org.obiba.opal.spi.analysis;

import java.util.ArrayList;
import java.util.List;
import org.obiba.magma.ValueTable;
import org.obiba.opal.spi.OpalServicePlugin;

public interface AnalysisService<T extends Analysis, U extends AnalysisResult> extends
    OpalServicePlugin {

  String SERVICE_TYPE = "opal-analysis";

  /**
   * Get the list of analysis that can be performed.
   *
   * @return
   */
  List<AnalysisTemplate> getAnalysisTemplates();

  /**
   * Perform a suite of analysis on the tibble representation of a {@link ValueTable} and report results.
   *
   * @param analyses
   * @return
   */
  List<U> analyse(List<T> analyses) throws NoSuchAnalysisTemplateException;


  /**
   * Perform an analysis on the tibble representation of a {@link ValueTable} and report result.
   *
   * @param analysis
   * @return
   */
  default U analyse(T analysis) throws NoSuchAnalysisTemplateException {
    List<T> list = new ArrayList<>();
    list.add(analysis);
    return analyse(list).get(0);
  }

}
