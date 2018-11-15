package org.obiba.opal.core.service;

public class NoSuchAnalysisException extends RuntimeException {

  private String analysisId;

  public String getAnalysisId() {
    return analysisId;
  }

  public NoSuchAnalysisException(String analysisId) {
    super("No Analysis with id \"" + analysisId +"\"");
    this.analysisId = analysisId;
  }
}
