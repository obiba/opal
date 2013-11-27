package org.obiba.opal.core.service;

import org.obiba.opal.core.domain.security.SubjectProfile;

/**
 * Thrown when attempting to add a user or a unit and a subject profile with same name already exists.
 */
public class DuplicateSubjectProfileException extends RuntimeException {

  private final SubjectProfile existing;

  public DuplicateSubjectProfileException(SubjectProfile existing) {
    super("A subject with name '" + existing.getPrincipal() + "' already exists in a different realm: " +
        existing.getRealm());
    this.existing = existing;
  }

  public SubjectProfile getExisting() {
    return existing;
  }
}
