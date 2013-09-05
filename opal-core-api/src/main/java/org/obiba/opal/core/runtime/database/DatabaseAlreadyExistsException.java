package org.obiba.opal.core.runtime.database;

public class DatabaseAlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = 2416927981990092192L;

  private final String name;

  public DatabaseAlreadyExistsException(String name) {
    super("Database with name '" + name + "' already exists in Opal.");
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
