package org.obiba.opal.core.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.ConstraintViolationException;
import org.obiba.opal.core.domain.OpalAnalysisResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpalAnalysisResultServiceImpl implements OpalAnalysisResultService {

  private final OrientDbService orientDbService;

  @Autowired
  public OpalAnalysisResultServiceImpl(OrientDbService orientDbService) {
    this.orientDbService = orientDbService;
  }

  @Override
  public OpalAnalysisResult getAnalysisResult(String id) {
    return orientDbService.uniqueResult(OpalAnalysisResult.class, "select from " + OpalAnalysisResult.class.getSimpleName() + " where id = ?", id);
  }

  @Override
  public Iterable<OpalAnalysisResult> getAnalysisResults() {
    return orientDbService.list(OpalAnalysisResult.class);
  }

  @Override
  public Iterable<OpalAnalysisResult> getAnalysisResults(String analysisId)
      throws NoSuchAnalysisException {
    return orientDbService.list(OpalAnalysisResult.class, "select from " + OpalAnalysisResult.class.getSimpleName() + " where analysis.analysisId = ?", analysisId);
  }

  @Override
  public void save(OpalAnalysisResult analysisResult) throws ConstraintViolationException {
    orientDbService.save(analysisResult, analysisResult);
  }

  @Override
  public void delete(OpalAnalysisResult analysisResult) throws NoSuchAnalysisResultException {
    orientDbService.delete(analysisResult);
  }

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(OpalAnalysisResult.class);
  }

  @Override
  @PreDestroy
  public void stop() {

  }
}
