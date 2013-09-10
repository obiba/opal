package org.obiba.opal.core.runtime.database;

public class CannotDeleteDatabaseWithDataException extends RuntimeException {

  private static final long serialVersionUID = 2416927981990092192L;

  private final String database;

  public CannotDeleteDatabaseWithDataException(String database) {
    super("Cannot delete database '" + database + "' because it has data.");
    this.database = database;
  }

  public String getDatabase() {
    return database;
  }
}
