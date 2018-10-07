package org.obiba.opal.spi.analysis;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

/**
 * Result of the analysis: status and report.
 */
public interface AnalysisResult<T extends Analysis> {

  /**
   * The original analysis request.
   *
   * @return
   */
  T getAnalysis();

  /**
   * Get the status of the analysis.
   *
   * @return
   */
  AnalysisStatus getStatus();

  /**
   * Get analysis associated message (may be empty), Markdown format is supported.
   *
   * @return
   */
  String getMessage();

  /**
   * Date at which the analysis was started.
   *
   * @return
   */
  Date getStartDate();

  /**
   * Date at which the analysis ended.
   *
   * @return
   */
  Date getEndDate();

  /**
   * Get if there are sub-results.
   *
   * @return
   */
  boolean hasSubResults();

  /**
   * Depending on the nature of the analysis, there could be some sub-results (for instance validation of several variables
   * gives one result per variable and a global result).
   *
   * @return
   */
  @Nullable List<AnalysisResult<T>> getSubResults();

  /**
   * Get ellapsed time.
   *
   * @return
   */
  default long getTime() {
    if (getStartDate() == null) return 0;
    if (getEndDate() == null) return new Date().getTime() - getStartDate().getTime();

    return getEndDate().getTime() - getStartDate().getTime();
  }

}
