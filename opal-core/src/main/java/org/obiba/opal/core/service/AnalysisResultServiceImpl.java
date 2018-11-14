package org.obiba.opal.core.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.ConstraintViolationException;
import org.obiba.opal.core.domain.AnalysisResultWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AnalysisResultServiceImpl implements AnalysisResultService {

  private final OrientDbService orientDbService;

  @Autowired
  public AnalysisResultServiceImpl(OrientDbService orientDbService) {
    this.orientDbService = orientDbService;
  }

  @Override
  public AnalysisResultWrapper getAnalysisResult(String id) {
    return orientDbService.uniqueResult(AnalysisResultWrapper.class, "select from " + AnalysisResultWrapper.class.getSimpleName() + " where id = ?", id);
  }

  @Override
  public Iterable<AnalysisResultWrapper> getAnalysisResults() {
    return orientDbService.list(AnalysisResultWrapper.class);
  }

  @Override
  public Iterable<AnalysisResultWrapper> getAnalysisResults(String analysisId)
      throws NoSuchAnalysisException {
    return orientDbService.list(AnalysisResultWrapper.class, "select from " + AnalysisResultWrapper.class.getSimpleName() + " where analysis.analysisId = ?", analysisId);
  }

  @Override
  public void save(AnalysisResultWrapper analysisResult) throws ConstraintViolationException {
    orientDbService.save(analysisResult, analysisResult);
  }

  @Override
  public void delete(AnalysisResultWrapper analysisResult) throws NoSuchAnalysisResultException {
    orientDbService.delete(analysisResult);
  }

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(AnalysisResultWrapper.class);
  }

  @Override
  @PreDestroy
  public void stop() {

  }
}
