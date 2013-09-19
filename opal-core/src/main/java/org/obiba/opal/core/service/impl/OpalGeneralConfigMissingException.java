package org.obiba.opal.core.service.impl;

public class OpalGeneralConfigMissingException extends RuntimeException {

  private static final long serialVersionUID = 2416927981990092192L;

  public OpalGeneralConfigMissingException() {
    super("Opal general configuration is missing");
  }
}
