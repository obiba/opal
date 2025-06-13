/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.shell;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.web.model.Commands.CommandStateDto.Status;
import org.obiba.opal.web.model.Commands.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Contains a command and the state of its execution.
 */
public class CommandJob implements OpalShell, Runnable {

  private static final Logger log = LoggerFactory.getLogger(CommandJob.class);

  //
  // Constants
  //

  private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH'h'mm";

  //
  // Instance Variables
  //

  private final String name;

  private final Command<?> command;

  private final List<Message> messages;

  private Integer id;

  private String owner;

  private Status status;

  private long submitTime;

  private Long startTime;

  private Long endTime;

  private String project;

  private String messageProgress;

  private Long currentProgress;

  private Long endProgress;

  private Integer percentProgress;

  //
  // CommandJob
  //

  public CommandJob(Command<?> command) {
    this(command.getName(), command);
  }

  public CommandJob(String name, Command<?> command) {
    if(command == null) throw new IllegalArgumentException("command cannot be null");
    this.name = name;
    this.command = command;
    this.command.setShell(this);
    messages = new ArrayList<>();
    status = Status.NOT_STARTED;
  }

  //
  // OpalShell Methods
  //

  @Override
  public void printf(String format, Object... args) {
    if(format == null) throw new IllegalArgumentException("format cannot be null");
    messages.add(createMessage(String.format(format, args)));
  }

  @Override
  public void progress(String message, long current, long end, int percent) {
    Session session = SecurityUtils.getSubject().getSession(false);
    if(session != null) session.touch();
    if (percent == 100) {
      String strMessage = Strings.isNullOrEmpty(message) ? name + " completed." : message;
      messages.add(createMessage(strMessage));
    }
    messageProgress = message;
    currentProgress = current;
    endProgress = end;
    percentProgress = percent;
  }

  @Override
  public void printUsage() {
    // nothing to do
  }

  @Override
  public char[] passwordPrompt(String format, Object... args) {
    // nothing to do -- return null
    return null;
  }

  @Override
  public String prompt(String format, Object... args) {
    // nothing to do -- return null
    return null;
  }

  @Override
  public void exit() {
    // nothing to do
  }

  @Override
  public void addExitCallback(OpalShellExitCallback callback) {
    // nothing to do
  }

  //
  // Runnable Methods
  //

  @Override
  public void run() {
    try {
      printf("Job started.");

      int errorCode = 0;

      // Don't execute the command if the task has been cancelled.
      if(status != Status.CANCEL_PENDING) {
        status = Status.IN_PROGRESS;
        startTime = getCurrentTime();
        errorCode = command.execute();
      }

      updateJobStatus(errorCode);
      printCompletion();
    } catch(Throwable t) {
      status = Status.FAILED;
      printf("Job has failed due to the following error :\n%s", t.getMessage());
      log.warn("Job threw an unexpected exception during execution.", t);
    } finally {
      endTime = getCurrentTime();
    }
  }

  //
  // Methods
  //

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public Command<?> getCommand() {
    return command;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
    this.command.setOwner(owner);
  }

  public boolean hasProject() {
    return project != null;
  }

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Date getSubmitTime() {
    return new Date(submitTime);
  }

  public void setSubmitTime(Date submitTime) {
    this.submitTime = submitTime.getTime();
  }

  public Date getStartTime() {
    return startTime == null ? null : new Date(startTime);
  }

  public String getStartTimeAsString() {
    return formatTime(getStartTime());
  }

  public Date getEndTime() {
    return endTime != null ? new Date(endTime) : null;
  }

  public String getEndTimeAsString() {
    return formatTime(getEndTime());
  }

  public List<Message> getMessages() {
    return Lists.newArrayList(messages);
  }

  public String getMessageProgress() {
    return messageProgress;
  }

  public Long getCurrentProgress() {
    return currentProgress;
  }

  public Long getEndProgress() {
    return endProgress;
  }

  public Integer getPercentProgress() {
    return percentProgress;
  }

  protected long getCurrentTime() {
    return System.currentTimeMillis();
  }

  protected Message createMessage(String msg) {
    return Message.newBuilder().setMsg(msg).setTimestamp(System.currentTimeMillis()).build();
  }

  protected String formatTime(Date date) {
    if(date == null) return null;
    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
    return dateFormat.format(date);
  }

  private void updateJobStatus(int errorCode) {
    // Update the status. Set to SUCCEEDED/FAILED, based on the error code, unless the status was changed to
    // CANCEL_PENDING (i.e., task was interrupted); in that case set it to CANCELED.
    switch(status) {
      case IN_PROGRESS:
        status = errorCode == 0 ? Status.SUCCEEDED : Status.FAILED;
        break;
      case CANCEL_PENDING:
        status = Status.CANCELED;
        break;
      default:
        // Should never get here!
        throw new IllegalStateException("Unexpected CommandJob status: " + status);
    }
  }

  private void printCompletion() {
    switch(status) {
      case CANCELED:
        printf("Job was canceled.");
        break;
      case FAILED:
        printf("Job failed.");
        break;
      case SUCCEEDED:
        printf("Job completed successfully.");
        break;
    }
  }
}
