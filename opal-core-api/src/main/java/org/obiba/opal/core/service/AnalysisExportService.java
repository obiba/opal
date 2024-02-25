/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
   * @param analysisName
   * @param outputStream
   * @param lastResult
   * @throws IOException
   */
  void exportProjectAnalysis(@NotNull String projectName,
                             @NotNull String tableName,
                             @NotNull String analysisName,
                             @NotNull OutputStream outputStream,
                             boolean lastResult) throws IOException;

  /**
   * Exports a specific result
   *
   * @param analysisName
   * @param resultId
   * @param outputStream
   * @throws IOException
   */
  void exportProjectAnalysisResult(@NotNull String projectName,
                                   @NotNull String tableName,
                                   @NotNull String analysisName,
                                   @NotNull String resultId,
                                   @NotNull OutputStream outputStream) throws IOException;

  /**
   * Export a specific result's report file(s)
   *
   * @param projectName
   * @param tableName
   * @param analysisName
   * @param resultId
   * @param outputStream
   * @throws IOException
   */
  void exportProjectAnalysisResultReport(@NotNull String projectName,
                                           @NotNull String tableName,
                                           @NotNull String analysisName,
                                           @NotNull String resultId,
                                           @NotNull OutputStream outputStream) throws IOException;
}
