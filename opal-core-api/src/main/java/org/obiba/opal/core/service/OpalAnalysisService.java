package org.obiba.opal.core.service;

import org.obiba.opal.core.domain.OpalAnalysis;

public interface OpalAnalysisService extends SystemService {

  OpalAnalysis getAnalysis(String id) throws NoSuchAnalysisException;

  Iterable<OpalAnalysis> getAnalyses();

  Iterable<OpalAnalysis> getAnalysesByDatasource(String datasource, String order, int limit);

  Iterable<OpalAnalysis> getAnalysesByDatasourceAndTable(String datasource, String table, String order, int limit);

  void save(OpalAnalysis analysis);

  void delete(OpalAnalysis analysis) throws NoSuchAnalysisException;
}
