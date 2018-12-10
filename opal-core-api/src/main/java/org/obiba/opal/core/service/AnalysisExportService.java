package org.obiba.opal.core.service;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Service for analysis export operations. All analyses files and results are archived in a ZIP file.
 */
public interface AnalysisExportService {
  /**
   * Exports all analyses of a project.
   *
   * @param projectName
   * @param outputStream
   * @param lastResult   - if 'false' all results are exported
   * @throws IOException
   */
  void exportProjectAnalyses(@NotNull String projectName,
                             @NotNull OutputStream outputStream,
                             boolean lastResult) throws IOException;

  /**
   * Exports all analyses of a project table.
   *
   * @param projectName
   * @param tableName
   * @param outputStream
   * @param lastResult   - if 'false' all results are exported
   * @throws IOException
   */
  void exportProjectTableAnalyses(@NotNull String projectName,
                                  @NotNull String tableName,
                                  @NotNull OutputStream outputStream,
                                  boolean lastResult) throws IOException;

  /**
   * Exports a specific analysis.
   *
   * @param analysisId
   * @param outputStream
   * @param lastResult
   * @throws IOException
   */
  void exportProjectAnalysis(@NotNull String projectName,
                             @NotNull String tableName,
                             @NotNull String analysisId,
                             @NotNull OutputStream outputStream,
                             boolean lastResult) throws IOException;

  /**
   * Exports a specific result
   *
   * @param analysisId
   * @param resultId
   * @param outputStream
   * @throws IOException
   */
  void exportProjectAnalysisResult(@NotNull String projectName,
                                   @NotNull String tableName,
                                   @NotNull String analysisId,
                                   @NotNull String resultId,
                                   @NotNull OutputStream outputStream) throws IOException;
}
