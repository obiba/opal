package org.obiba.opal.core.service;

import com.sun.istack.Nullable;
import org.obiba.opal.core.domain.OpalAnalysisResult;
import org.obiba.opal.core.tools.SimpleOrientDbQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.ConstraintViolationException;

@Component
public class OpalAnalysisResultServiceImpl implements OpalAnalysisResultService {

  private final OrientDbService orientDbService;

  @Autowired
  public OpalAnalysisResultServiceImpl(OrientDbService orientDbService) {
    this.orientDbService = orientDbService;
  }

  @Override
  public OpalAnalysisResult getAnalysisResult(String analysisId, String resultId) {
    String query = SimpleOrientDbQueryBuilder.newInstance()
      .table(OpalAnalysisResult.class.getSimpleName())
      .whereClauses("analysisId = ?" ,"id = ?")
      .build();

    return orientDbService.uniqueResult(OpalAnalysisResult.class, query, analysisId, resultId);
  }

  @Override
  public Iterable<OpalAnalysisResult> getAnalysisResults(@Nullable String order, int limit) {
    String query = SimpleOrientDbQueryBuilder.newInstance()
      .table(OpalAnalysisResult.class.getSimpleName())
      .order(order)
      .limit(limit)
      .build();

    return orientDbService.list(OpalAnalysisResult.class, query);
  }

  @Override
  public Iterable<OpalAnalysisResult> getAnalysisResults(String analysisId, String order, int limit)
      throws NoSuchAnalysisException {
    String query = SimpleOrientDbQueryBuilder.newInstance()
      .table(OpalAnalysisResult.class.getSimpleName())
      .whereClauses("analysisId = ?")
      .order(order)
      .limit(limit)
      .build();

    return orientDbService.list(OpalAnalysisResult.class, query, analysisId);
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
