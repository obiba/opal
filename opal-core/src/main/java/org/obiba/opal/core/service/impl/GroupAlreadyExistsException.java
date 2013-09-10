package org.obiba.opal.core.service.impl;

public class GroupAlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = 2416927981990092192L;

  private final String existing;

  public GroupAlreadyExistsException(String existing) {
    super("Group named '" + existing + "' already exists.");
    this.existing = existing;
  }

  public String getExisting() {
    return existing;
  }
}
