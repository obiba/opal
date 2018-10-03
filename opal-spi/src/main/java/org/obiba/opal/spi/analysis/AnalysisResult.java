package org.obiba.opal.spi.analysis;

/**
 * Result of the analysis: status and report.
 */
public interface AnalysisResult {

  /**
   * The original analysis request.
   *
   * @return
   */
  Analysis getAnalysis();

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
   * Path in Opal file system to the analysis report, supported formats are html and pdf.
   *
   * @return
   */
  String getReportPath();

}
