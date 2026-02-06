package org.obiba.opal.r.service.tasks;

import org.obiba.opal.r.service.RServerSession;

public class RSessionStateWaiter implements Runnable {

  private final RServerSession rSession;
  private final String restore;

  public RSessionStateWaiter(RServerSession rSession) {
    this(rSession, null);
  }

  public RSessionStateWaiter(RServerSession rSession, String restore) {
    this.rSession = rSession;
    this.restore = restore;
  }

  @Override
  public void run() {
    while (RServerSession.State.PENDING.equals(rSession.getState())) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
    if (RServerSession.State.RUNNING.equals(rSession.getState())) {
      onRunning(this.rSession, this.restore);
    }
  }

  /**
   * Called once the R session is in Running state (accepts R operations).
   *
   * @param rSession
   * @param restore
   */
  protected void onRunning(RServerSession rSession, String restore) {

  }
}
