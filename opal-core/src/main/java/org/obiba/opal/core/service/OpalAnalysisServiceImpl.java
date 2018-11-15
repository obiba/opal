package org.obiba.opal.core.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.obiba.opal.core.domain.OpalAnalysis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpalAnalysisServiceImpl implements OpalAnalysisService {

  private final OrientDbService orientDbService;

  @Autowired
  public OpalAnalysisServiceImpl(OrientDbService orientDbService) {
    this.orientDbService = orientDbService;
  }

  @Override
  public OpalAnalysis getAnalysis(String id) throws NoSuchAnalysisException {
    return orientDbService.uniqueResult(OpalAnalysis.class, "select * from " + OpalAnalysis.class.getSimpleName() + " where id = ?", id);
  }

  @Override
  public Iterable<OpalAnalysis> getAnalyses() {
    return orientDbService.list(OpalAnalysis.class);
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
