/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

import com.google.common.base.Strings;

import java.io.File;
import java.util.Date;

/**
 * Defines the common attributes of what is a R server session.
 */
public abstract class AbstractRServerSession implements RServerSession {

  private final String id;

  private final String user;

  private final Date created;

  private Date timestamp;

  private boolean busy = false;

  private String executionContext = DEFAULT_CONTEXT;

  protected AbstractRServerSession(String id, String user) {
    this.id = id;
    this.user = user;
    this.created = new Date();
    this.timestamp = created;
  }

  //
  // RServerSession methods
  //

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void touch() {
    timestamp = new Date();
  }

  @Override
  public String getUser() {
    return user;
  }

  @Override
  public Date getCreated() {
    return created;
  }

  @Override
  public Date getTimestamp() {
    return timestamp;
  }

  @Override
  public boolean isBusy() {
    return busy;
  }

  @Override
  public void setExecutionContext(String executionContext) {
    this.executionContext = executionContext;
  }

  @Override
  public String getExecutionContext() {
    return Strings.isNullOrEmpty(executionContext) ? DEFAULT_CONTEXT : executionContext;
  }

  /**
   * Check if the R session is not busy and has expired.
   *
   * @param timeout in minutes
   * @return
   */
  public boolean hasExpired(long timeout) {
    Date now = new Date();
    return !busy && now.getTime() - timestamp.getTime() > timeout * 60 * 1000;
  }

  //
  // RASyncOperationTemplate
  //

  //
  // Protected methods
  //

  protected synchronized void setBusy(boolean busy) {
    this.busy = busy;
  }

  /**
   * Get the workspaces directory for the current execution context.
   *
   * @return
   */
  protected File getWorkspaces() {
    return new File(String.format(String.format(OpalRSessionManager.WORKSPACES_FORMAT, getExecutionContext())));
  }

}
