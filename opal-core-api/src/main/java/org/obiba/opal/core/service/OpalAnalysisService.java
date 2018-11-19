package org.obiba.opal.core.service;

import org.obiba.opal.core.domain.OpalAnalysis;

public interface OpalAnalysisService extends SystemService {

  OpalAnalysis getAnalysis(String id) throws NoSuchAnalysisException;

  Iterable<OpalAnalysis> getAnalyses();

  Iterable<OpalAnalysis> getAnalysesByDatasource(String datasource);

  Iterable<OpalAnalysis> getAnalysesByDatasourceAndTable(String datasource, String table);

  void save(OpalAnalysis analysis);

  void delete(OpalAnalysis analysis) throws NoSuchAnalysisException;
}
