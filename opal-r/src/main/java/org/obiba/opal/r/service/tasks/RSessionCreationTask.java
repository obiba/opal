package org.obiba.opal.r.service.tasks;

import org.obiba.opal.r.service.RServerProfile;

public class RSessionCreationTask extends RSessionTask {

  private final SubjectRSessions rSessions;

  public RSessionCreationTask(String id, String principal, RServerProfile profile, SubjectRSessions rSessions) {
    super(id, principal, profile);
    this.rSessions = rSessions;
  }

  public SubjectRSessions getRSessions() {
    return rSessions;
  }
}

