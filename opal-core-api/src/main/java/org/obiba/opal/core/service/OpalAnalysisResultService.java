package org.obiba.opal.core.service;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.sun.istack.Nullable;
import org.obiba.opal.core.domain.OpalAnalysisResult;

public interface OpalAnalysisResultService extends SystemService {

  OpalAnalysisResult getAnalysisResult(String analysisId, String resultId);

  Iterable<OpalAnalysisResult> getAnalysisResults(boolean lastResult);

  Iterable<OpalAnalysisResult> getAnalysisResults(@NotNull String analysisId, boolean lastResult) throws NoSuchAnalysisException;

  void save(@NotNull OpalAnalysisResult analysisResult) throws ConstraintViolationException;

  void delete(@NotNull OpalAnalysisResult analysisResult) throws NoSuchAnalysisResultException;

}
