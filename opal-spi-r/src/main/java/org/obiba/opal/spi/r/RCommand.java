/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r;

import com.google.common.base.Strings;

import java.util.Date;

/**
 * A R command is for deferred execution of an ROperation.
 */
public class RCommand {
  public enum Status {
    PENDING, IN_PROGRESS, COMPLETED, FAILED
  }

  private final String id;

  private final ROperation rOperation;

  private Status status;

  private final Date createDate;

  private Date startDate;

  private Date endDate;

  private String error;

  public RCommand(String id, ROperation rOperation) {
    this.id = id;
    this.rOperation = rOperation;
    status = Status.PENDING;
    createDate = new Date();
  }

  public String getId() {
    return id;
  }

  public ROperation getROperation() {
    return rOperation;
  }

  public Status getStatus() {
    return status;
  }

  public boolean isFinished() {
    return status == Status.COMPLETED || status == Status.FAILED;
  }

  public Date getCreateDate() {
    return createDate;
  }

  public Date getStartDate() {
    return startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public boolean hasError() {
    return !Strings.isNullOrEmpty(error);
  }

  public String getError() {
    return error;
  }

  public boolean hasResult() {
    return rOperation instanceof ROperationWithResult && asROperationWithResult().hasResult();
  }

  public ROperationWithResult asROperationWithResult() {
    return (ROperationWithResult) rOperation;
  }

  public void inProgress() {
    status = Status.IN_PROGRESS;
    startDate = new Date();
  }

  public void completed() {
    status = Status.COMPLETED;
    endDate = new Date();
  }

  public void failed(String message) {
    status = Status.FAILED;
    endDate = new Date();
    error = message;
  }

  @Override
  public String toString() {
    return rOperation.toString();
  }
}
