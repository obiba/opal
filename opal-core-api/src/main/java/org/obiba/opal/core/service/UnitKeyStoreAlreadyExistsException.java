package org.obiba.opal.core.service;

public class UnitKeyStoreAlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = 2416927981990092192L;

  private final String existing;

  public UnitKeyStoreAlreadyExistsException(String existing) {
    super("UnitKeyStore for unit '" + existing + "' already exists.");
    this.existing = existing;
  }

  public String getExisting() {
    return existing;
  }
}
