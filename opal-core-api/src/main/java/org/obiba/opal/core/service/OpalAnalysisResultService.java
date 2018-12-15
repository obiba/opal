package org.obiba.opal.core.service;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import org.obiba.opal.core.domain.OpalAnalysisResult;

public interface OpalAnalysisResultService extends SystemService {

  OpalAnalysisResult getAnalysisResult(String analysisName, String resultId);

  Iterable<OpalAnalysisResult> getAnalysisResults(boolean lastResult);

  Iterable<OpalAnalysisResult> getAnalysisResults(@NotNull String datasource, @NotNull String table, @NotNull String analysisName, boolean lastResult) throws NoSuchAnalysisException;

  void save(@NotNull OpalAnalysisResult analysisResult) throws ConstraintViolationException;

  void delete(@NotNull OpalAnalysisResult analysisResult) throws NoSuchAnalysisResultException;

}
