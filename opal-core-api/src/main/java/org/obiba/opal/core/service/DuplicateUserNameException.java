package org.obiba.opal.core.service;

import org.obiba.opal.core.domain.user.SubjectCredentials;

/**
 * Thrown when attempting to add an already existing user.
 */
//TODO replace this by ConstraintViolationException
@Deprecated
public class DuplicateUserNameException extends RuntimeException {

  private final SubjectCredentials existing;

  private final SubjectCredentials duplicate;

  public DuplicateUserNameException(SubjectCredentials existing, SubjectCredentials duplicate) {
    super("A user with same name already exists: " + existing.getName());
    this.existing = existing;
    this.duplicate = duplicate;
  }

  public SubjectCredentials getExisting() {
    return existing;
  }

  public SubjectCredentials getDuplicate() {
    return duplicate;
  }
}
