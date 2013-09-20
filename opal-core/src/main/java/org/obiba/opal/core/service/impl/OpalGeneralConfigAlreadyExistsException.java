package org.obiba.opal.core.service.impl;

public class OpalGeneralConfigAlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = 2416927981990092192L;

  public OpalGeneralConfigAlreadyExistsException() {
    super("Opal general configuration already exists");
  }
}
