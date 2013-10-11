package org.obiba.opal.project;

public class ProjectAlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = 2416927981990092192L;

  private final String existing;

  public ProjectAlreadyExistsException(String existing) {
    super("Project named '" + existing + "' already exists.");
    this.existing = existing;
  }

  public String getExisting() {
    return existing;
  }
}
