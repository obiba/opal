package org.obiba.opal.core.service;

import com.sun.istack.Nullable;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.OutputStream;

public interface AnalysisExportService {
  void exportProjectAnalysis(@NotNull String projectName, @NotNull OutputStream outputStream) throws IOException;

  void exportProjectAnalysis(@NotNull String projectName, @NotNull OutputStream outputStream, @Nullable String tableName) throws IOException;
}
