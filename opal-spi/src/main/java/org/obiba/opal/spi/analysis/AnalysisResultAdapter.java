package org.obiba.opal.spi.analysis;

import java.util.Date;

/**
 * Convenient class for implementing data processing engine specific results.
 *
 */
public abstract class AnalysisResultAdapter<T extends Analysis> implements AnalysisResult {

  private final T analysis;

  private AnalysisStatus status;

  private String message;

  private Date startDate;

  private Date endDate;

  protected AnalysisResultAdapter(T analysis) {
    this.analysis = analysis;
  }

  @Override
  public T getAnalysis() {
    return analysis;
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
}
