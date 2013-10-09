package org.obiba.opal.core.runtime.taxonomy;

public class TaxonomyAlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = 2416927981990092192L;

  private final String existing;

  public TaxonomyAlreadyExistsException(String existing) {
    super("Taxonomy named '" + existing + "' already exists.");
    this.existing = existing;
  }

  public String getExisting() {
    return existing;
  }
}
