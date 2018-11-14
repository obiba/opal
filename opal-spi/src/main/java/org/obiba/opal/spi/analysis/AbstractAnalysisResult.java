package org.obiba.opal.spi.analysis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Convenient class for implementing data processing engine specific results.
 *
 */
public abstract class AbstractAnalysisResult<T extends Analysis> implements AnalysisResult {

  private String id;

  private final String analysisId;

  private AnalysisStatus status;

  private String message;

  private Date startDate;

  private Date endDate;

  private List<AnalysisResult<T>> resultItems;

  protected AbstractAnalysisResult(T analysis) {
    this.id = UUID.randomUUID().toString();
    this.analysisId = analysis.getId();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getAnalysisId() {
    return analysisId;
  }

  protected void setStatus(AnalysisStatus status) {
    this.status = status;
  }

  @Override
  public AnalysisStatus getStatus() {
    return status;
  }

  protected void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message;
  }

  protected void setStartDate(Date startDate) {
    this.startDate = startDate;
    status = AnalysisStatus.IN_PROGRESS;
  }

  @Override
  public Date getStartDate() {
    return startDate;
  }

  protected void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  @Override
  public Date getEndDate() {
    return endDate;
  }

  @Override
  public boolean hasResultItems() {
    return resultItems != null && !resultItems.isEmpty();
  }

  @Override
  public List<AnalysisResult<T>> getResultItems() {
    return resultItems == null ? resultItems = new ArrayList<>() : resultItems;
  }

  protected void addResultItems(List<AnalysisResult<T>> results) {
    getResultItems().addAll(results);
  }

  protected void addResultItem(AnalysisResult<T> result) {
    getResultItems().add(result);
  }
}
