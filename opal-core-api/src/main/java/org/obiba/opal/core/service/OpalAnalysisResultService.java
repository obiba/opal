package org.obiba.opal.core.service;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import org.obiba.opal.core.domain.OpalAnalysisResult;

public interface OpalAnalysisResultService extends SystemService {

  OpalAnalysisResult getAnalysisResult(String id);

  Iterable<OpalAnalysisResult> getAnalysisResults();

  Iterable<OpalAnalysisResult> getAnalysisResults(@NotNull String analysisId) throws NoSuchAnalysisException;

  void save(@NotNull OpalAnalysisResult analysisResult) throws ConstraintViolationException;

  void delete(@NotNull OpalAnalysisResult analysisResult) throws NoSuchAnalysisResultException;

}
