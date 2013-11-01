package org.obiba.opal.core.service.database;

public class IdentifiersDatabaseNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 2416927981990092192L;

  public IdentifiersDatabaseNotFoundException() {
    super("Database for identifiers is not defined");
  }
}
