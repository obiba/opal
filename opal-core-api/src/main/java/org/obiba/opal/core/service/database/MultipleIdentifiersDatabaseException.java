package org.obiba.opal.core.service.database;

public class MultipleIdentifiersDatabaseException extends RuntimeException {

  private static final long serialVersionUID = 2416927981990092192L;

  private final String existing;

  private final String database;

  public MultipleIdentifiersDatabaseException(String existing, String database) {
    super("Database for identifiers already exists: '" + existing + "'.");
    this.existing = existing;
    this.database = database;
  }

  public String getDatabase() {
    return database;
  }

  public String getExisting() {
    return existing;
  }
}
