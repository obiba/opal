package org.obiba.opal.core.runtime.taxonomy;

public class VocabularyAlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = 2416927981990092192L;

  private final String existing;

  public VocabularyAlreadyExistsException(String existing) {
    super("Vocabulary named '" + existing + "' already exists.");
    this.existing = existing;
  }

  public String getExisting() {
    return existing;
  }
}
