package org.obiba.opal.core.service;

public class NoSuchAnalysisResultException extends RuntimeException {

  private String analysisResultId;

  public String getAnalysisResultId() {
    return analysisResultId;
  }

  public NoSuchAnalysisResultException(String analysisResultId) {
    super("No AnalysisResult with id \"" + analysisResultId +"\"");
    this.analysisResultId = analysisResultId;
  }
}
