package org.obiba.opal.core.service;

import com.sun.istack.Nullable;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.OutputStream;

public interface AnalysisExportService {
  void exportProjectAnalyses(@NotNull String projectName,
                             @NotNull OutputStream outputStream,
                             @Nullable String aOrder,
                             int aLimit,
                             @Nullable String rOrder,
                             int rLimit
                             ) throws IOException;

  void exportProjectAnalyses(@NotNull String projectName,
                             @NotNull OutputStream outputStream,
                             @Nullable String aOrder,
                             int aLimit,
                             @Nullable String rOrder,
                             int rLimit,
                             @Nullable String tableName) throws IOException;
}
