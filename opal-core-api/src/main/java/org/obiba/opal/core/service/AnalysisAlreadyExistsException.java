package org.obiba.opal.core.service;

public class AnalysisAlreadyExistsException extends RuntimeException {

  private String name;

  public String getName() {
    return name;
  }

  public AnalysisAlreadyExistsException(String name) {
    super("Analysis \"" + name + "\" already exists");
    this.name = name;
  }

}
