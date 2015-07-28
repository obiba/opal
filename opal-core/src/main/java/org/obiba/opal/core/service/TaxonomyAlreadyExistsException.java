package org.obiba.opal.core.service;

public class TaxonomyAlreadyExistsException extends RuntimeException {
  private static final long serialVersionUID = 6974217678162256985L;

  private String name;

  public TaxonomyAlreadyExistsException(String name) {
    super("A taxonomy with the name'" + name + "' already exists.");
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
