/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.analysis;

import org.obiba.opal.spi.analysis.AbstractAnalysisResult;
import org.obiba.opal.spi.analysis.AnalysisResultItem;
import org.obiba.opal.spi.analysis.AnalysisStatus;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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

    public Builder items(List<AnalysisResultItem> items) {
      result.addResultItems(items);
      return this;
    }

    public RAnalysisResult build() {
      return result;
    }

    public String getResultId() {
      return resultId;
    }
  }

  static class RAnalysisResultItem implements AnalysisResultItem {

    private final String message;
    private final AnalysisStatus status;

    RAnalysisResultItem(AnalysisStatus status, String message) {
      this.status = status;
      this.message = message;
    }

    @Override
    public AnalysisStatus getStatus() {
      return status;
    }

    @Override
    public String getMessage() {
      return message;
    }
  }
}
