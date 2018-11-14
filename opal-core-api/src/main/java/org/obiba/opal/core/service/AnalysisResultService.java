package org.obiba.opal.core.service;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import org.obiba.opal.core.domain.AnalysisResultWrapper;

public interface AnalysisResultService extends SystemService {

  AnalysisResultWrapper getAnalysisResult(String id);

  Iterable<AnalysisResultWrapper> getAnalysisResults();

  Iterable<AnalysisResultWrapper> getAnalysisResults(@NotNull String analysisId) throws NoSuchAnalysisException;

  void save(@NotNull AnalysisResultWrapper analysisResult) throws ConstraintViolationException;

  void delete(@NotNull AnalysisResultWrapper analysisResult) throws NoSuchAnalysisResultException;

}
