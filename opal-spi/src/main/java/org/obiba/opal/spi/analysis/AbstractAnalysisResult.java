package org.obiba.opal.spi.analysis;

import org.obiba.opal.spi.analysis.support.generator.IdGeneratorFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Convenient class for implementing data processing engine specific results.
 *
 */
public abstract class AbstractAnalysisResult<T extends Analysis> implements AnalysisResult {

  private String id;

  private final String analysisName;

  private AnalysisStatus status;

  private String message;

  private Date startDate;

  private Date endDate;

  private List<AnalysisResultItem> resultItems;

  protected AbstractAnalysisResult(T analysis) {
    this.id = IdGeneratorFactory.createDateIdGenerator().generate();
    this.analysisName = analysis.getName();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getAnalysisName() {
    return analysisName;
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
  public List<AnalysisResultItem> getResultItems() {
    return resultItems == null ? resultItems = new ArrayList<>() : resultItems;
  }

  protected void addResultItems(List<AnalysisResultItem> results) {
    getResultItems().addAll(results);
  }

  protected void addResultItem(AnalysisResultItem result) {
    getResultItems().add(result);
  }
}
