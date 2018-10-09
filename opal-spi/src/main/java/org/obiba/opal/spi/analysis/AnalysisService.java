package org.obiba.opal.spi.analysis;

import org.obiba.magma.ValueTable;
import org.obiba.plugins.spi.ServicePlugin;

import java.util.ArrayList;
import java.util.List;

public interface AnalysisService<T extends Analysis, U extends AnalysisResult> extends ServicePlugin  {

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
