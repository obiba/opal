package org.obiba.opal.core.runtime.database;

public class DatabaseAlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = 2416927981990092192L;

  private final String existing;

  public DatabaseAlreadyExistsException(String existing) {
    super("Database named '" + existing + "' already exists.");
    this.existing = existing;
  }

  public String getExisting() {
    return existing;
  }
}
