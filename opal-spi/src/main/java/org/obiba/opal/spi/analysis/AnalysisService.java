package org.obiba.opal.spi.analysis;

import com.google.common.collect.Lists;
import org.obiba.magma.ValueTable;
import org.obiba.plugins.spi.ServicePlugin;

import java.util.List;

public interface AnalysisService<T extends Analysis> extends ServicePlugin  {

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
  List<AnalysisResult> analyse(List<T> analyses) throws NoSuchAnalysisTemplateException;

  /**
   * Perform an analysis on the tibble representation of a {@link ValueTable} and report result.
   *
   * @param analysis
   * @return
   */
  default AnalysisResult analyse(T analysis) throws NoSuchAnalysisTemplateException {
    return analyse(Lists.newArrayList(analysis)).get(0);
  }
}
