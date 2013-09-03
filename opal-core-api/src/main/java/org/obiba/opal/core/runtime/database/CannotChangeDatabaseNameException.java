package org.obiba.opal.core.runtime.database;

public class CannotChangeDatabaseNameException extends RuntimeException {

  private static final long serialVersionUID = 2416927981990092192L;

  private final String previousName;

  private final String newName;

  public CannotChangeDatabaseNameException(String previousName, String newName) {
    super("Cannot change '" + previousName + "' database name");
    this.previousName = previousName;
    this.newName = newName;
  }

  public String getPreviousName() {
    return previousName;
  }

  public String getNewName() {
    return newName;
  }
}
