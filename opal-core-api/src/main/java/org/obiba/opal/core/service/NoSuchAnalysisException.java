package org.obiba.opal.core.service;

public class NoSuchAnalysisException extends RuntimeException {

  private String analysisName;

  public String getAnalysisName() {
    return analysisName;
  }

  public NoSuchAnalysisException(String analysisName) {
    super("No Analysis with name \"" + analysisName +"\"");
    this.analysisName = analysisName;
  }
}
