package org.obiba.opal.core.service;

import org.obiba.opal.core.domain.OpalAnalysis;

public interface OpalAnalysisService extends SystemService {

  OpalAnalysis getAnalysis(String id) throws NoSuchAnalysisException;

  Iterable<OpalAnalysis> getAnalyses();

  void save(OpalAnalysis analysis);

  void delete(OpalAnalysis analysis) throws NoSuchAnalysisException;
}
