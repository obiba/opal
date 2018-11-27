package org.obiba.opal.core.service;

import com.sun.istack.Nullable;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Service for analysis export operations. All analyses files and results are archived in a ZIP file.
 */
public interface AnalysisExportService {
  /**
   * Exports all analyses of a given project.
   *
   * @param projectName
   * @param outputStream
   * @param lastResult - if 'false' all results are exported
   * @throws IOException
   */
  void exportProjectAnalyses(@NotNull String projectName,
                             @NotNull OutputStream outputStream,
                             boolean lastResult
                             ) throws IOException;

  /**
   * Exports all analyses of a table in a given project.
   *
   * @param projectName
   * @param outputStream
   * @param lastResult
   * @param tableName
   * @throws IOException
   */
  void exportProjectAnalyses(@NotNull String projectName,
                             @NotNull OutputStream outputStream,
                             boolean lastResult,
                             @Nullable String tableName) throws IOException;
}
