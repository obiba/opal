package org.obiba.opal.spi.analysis;

import java.util.Date;

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
