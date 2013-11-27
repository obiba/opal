package org.obiba.opal.core.service;

import org.obiba.opal.core.domain.user.User;

/**
 * Thrown when attempting to add an already existing user.
 */
public class DuplicateUserNameException extends RuntimeException {

  private final User existing;

  private final User duplicate;

  public DuplicateUserNameException(User existing, User duplicate) {
    super("A user with same name already exists: " + existing.getName());
    this.existing = existing;
    this.duplicate = duplicate;
  }

  public User getExisting() {
    return existing;
  }

  public User getDuplicate() {
    return duplicate;
  }
}
