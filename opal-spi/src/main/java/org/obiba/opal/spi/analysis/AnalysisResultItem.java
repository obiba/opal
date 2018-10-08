package org.obiba.opal.spi.analysis;

public interface AnalysisResultItem {

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

}
