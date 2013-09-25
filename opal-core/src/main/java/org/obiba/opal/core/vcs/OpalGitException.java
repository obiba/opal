package org.obiba.opal.core.vcs;

public class OpalGitException extends RuntimeException {

  public OpalGitException(String msg) {
    super(msg);
  }

  public OpalGitException(String msg, Throwable cause) {
    super(msg, cause);
  }

}


