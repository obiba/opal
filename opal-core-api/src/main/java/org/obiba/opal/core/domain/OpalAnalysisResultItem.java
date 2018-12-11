package org.obiba.opal.core.domain;

import org.obiba.opal.spi.analysis.AnalysisResultItem;
import org.obiba.opal.spi.analysis.AnalysisStatus;

public class OpalAnalysisResultItem implements AnalysisResultItem {

  private String message;
  private AnalysisStatus status;

  OpalAnalysisResultItem() { }

  OpalAnalysisResultItem(AnalysisResultItem copy) {
    this.message = copy.getMessage();
    this.status = copy.getStatus();
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