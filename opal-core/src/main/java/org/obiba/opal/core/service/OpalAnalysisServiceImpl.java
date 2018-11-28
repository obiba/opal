package org.obiba.opal.core.service;

import org.obiba.opal.core.domain.OpalAnalysis;
import org.obiba.opal.core.tools.SimpleOrientDbQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class OpalAnalysisServiceImpl implements OpalAnalysisService {

  private final OrientDbService orientDbService;

  @Autowired
  public OpalAnalysisServiceImpl(OrientDbService orientDbService) {
    this.orientDbService = orientDbService;
  }

  @Override
  public OpalAnalysis getAnalysis(String id) throws NoSuchAnalysisException {
    String query = SimpleOrientDbQueryBuilder.newInstance()
      .table(OpalAnalysis.class.getSimpleName())
      .whereClauses("id = ?")
      .build();
    
    return orientDbService.uniqueResult(OpalAnalysis.class, query, id);
  }

  @Override
  public OpalAnalysis getAnalysisByDatasourceAndTableAndId(String datasource, String table, String id) {
    String query = SimpleOrientDbQueryBuilder.newInstance()
      .table(OpalAnalysis.class.getSimpleName())
      .whereClauses("datasource = ?", "table = ?", "id = ?")
      .build();
    return orientDbService.uniqueResult(OpalAnalysis.class, query, datasource, table, id);
  }

  @Override
  public Iterable<OpalAnalysis> getAnalyses() {
    return orientDbService.list(OpalAnalysis.class);
  }

  @Override
  public Iterable<OpalAnalysis> getAnalysesByDatasource(String datasource) {
    String query = SimpleOrientDbQueryBuilder.newInstance()
      .table(OpalAnalysis.class.getSimpleName())
      .whereClauses("datasource = ?")
      .build();

    return orientDbService.list(OpalAnalysis.class, query, datasource);
  }

  @Override
  public Iterable<OpalAnalysis> getAnalysesByDatasourceAndTable(String datasource,
                                                                String table) {
    String query = SimpleOrientDbQueryBuilder.newInstance()
      .table(OpalAnalysis.class.getSimpleName())
      .whereClauses("datasource = ?", "table = ?")
      .build();

    return orientDbService.list(OpalAnalysis.class, query, datasource, table);
  }

  @Override
  public void save(OpalAnalysis analysis) {
    orientDbService.save(analysis, analysis);
  }

  @Override
  public void delete(OpalAnalysis analysis) throws NoSuchAnalysisException {
    orientDbService.delete(analysis);
  }

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(OpalAnalysis.class);
  }

  @Override
  @PreDestroy
  public void stop() {

  }
}
