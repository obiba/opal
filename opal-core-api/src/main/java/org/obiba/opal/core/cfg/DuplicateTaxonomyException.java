package org.obiba.opal.core.cfg;

import org.obiba.opal.core.domain.taxonomy.Taxonomy;

public class DuplicateTaxonomyException extends RuntimeException {

  private final Taxonomy existing;

  public DuplicateTaxonomyException(Taxonomy existing) {
    super("A taxonomy with name '" + existing.getName() + "' already exists");
    this.existing = existing;
  }

  public Taxonomy getExisting() {
    return existing;
  }
}
