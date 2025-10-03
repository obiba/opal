package org.obiba.opal.r.service.tasks;

import com.google.common.base.Strings;
import org.apache.commons.compress.utils.Lists;
import org.obiba.opal.r.service.RServerProfile;

import java.util.Date;
import java.util.List;

public class RSessionTask {

  public enum Status {
    PENDING, IN_PROGRESS, COMPLETED, FAILED
  }

  private final String id;

  private final String principal;

  private final RServerProfile profile;

  private Status status;

  private final Date createDate;

  private Date startDate;

  private Date endDate;

  private String error;

  protected RSessionTask(String id, String principal, RServerProfile profile) {
    this.id = id;
    this.principal = principal;
    this.profile = profile;
    this.createDate = new Date();
    this.status = Status.PENDING;
  }

  public String getId() {
    return id;
  }

  public String getPrincipal() {
    return principal;
  }

  public RServerProfile getProfile() {
    return profile;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
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

  public String getError() {
    return error;
  }

  public void inProgress() {
    this.startDate = new Date();
    this.status = Status.IN_PROGRESS;
  }

  public void completed() {
    this.endDate = new Date();
    this.status = Status.COMPLETED;
  }

  public void failed(String message) {
    this.error = message;
    this.endDate = new Date();
    this.status = Status.FAILED;
  }
}
