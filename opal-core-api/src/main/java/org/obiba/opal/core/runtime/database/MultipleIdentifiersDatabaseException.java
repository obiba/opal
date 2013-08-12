package org.obiba.opal.core.runtime.database;

import org.obiba.opal.core.domain.database.Database;

public class MultipleIdentifiersDatabaseException extends RuntimeException {

  private static final long serialVersionUID = 2416927981990092192L;

  private final Database existing;

  private final Database database;

  public MultipleIdentifiersDatabaseException(Database existing, Database database) {
    super("Database for identifiers already exists: '" + existing.getName() + "'.");
    this.existing = existing;
    this.database = database;
  }

  public Database getDatabase() {
    return database;
  }

  public Database getExisting() {
    return existing;
  }
}
