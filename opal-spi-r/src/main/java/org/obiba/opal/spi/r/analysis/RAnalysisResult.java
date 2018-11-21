package org.obiba.opal.spi.r.analysis;

import org.obiba.opal.spi.analysis.AbstractAnalysisResult;
import org.obiba.opal.spi.analysis.AnalysisStatus;

import java.util.Date;

public class RAnalysisResult extends AbstractAnalysisResult<RAnalysis> {

  private String reportPath;

  private RAnalysisResult(RAnalysis analysis) {
    super(analysis);
  }

  /**
   * Path in Opal file system to the analysis report, supported formats are html and pdf.
   *
   * @return
   */
  public String getReportPath() {
    return reportPath;
  }

  public Builder builder() {
    return new Builder(this);
  }

  public static Builder create(RAnalysis analysis) {
    return new Builder(analysis);
  }

  public static class Builder {
    private String resultId;
    private RAnalysisResult result;

    private Builder(RAnalysis analysis) {
      this.result = new RAnalysisResult(analysis);
      this.resultId = result.getId();
    }

    public Builder(RAnalysisResult result) {
      this.result = result;
    }

    public Builder status(AnalysisStatus status) {
      result.setStatus(status);
      return this;
    }

    public Builder status(String status) {
      result.setStatus(AnalysisStatus.valueOf(status.toUpperCase()));
      return this;
    }

    public Builder message(String message) {
      result.setMessage(message);
      return this;
    }

    public Builder start() {
      result.setStartDate(new Date());
      return this;
    }

    public Builder end() {
      result.setEndDate(new Date());
      return this;
    }

    public Builder report(String path) {
      result.reportPath = path;
      return this;
    }

    public RAnalysisResult build() {
      return result;
    }

    public String getResultId() {
      return resultId;
    }
  }
}
