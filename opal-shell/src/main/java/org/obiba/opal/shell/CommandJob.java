/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.List;

import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.web.model.Commands.Message;
import org.obiba.opal.web.model.Commands.CommandStateDto.Status;

/**
 * Contains a command and the state of its execution.
 */
public class CommandJob implements OpalShell, Runnable {
  //
  // Instance Variables
  //

  private Command<?> command;

  private String owner;

  private Status status;

  private Date submitTime;

  private Date startTime;

  private Date endTime;

  private List<Message> messages;

  //
  // CommandJob
  //

  public CommandJob() {
    messages = new ArrayList<Message>();
  }

  //
  // OpalShell Methods
  //

  public void printf(String format, Object... args) {
    StringBuffer sb = new StringBuffer();
    Formatter formatter = new Formatter(sb);
    formatter.format(format, args);

    messages.add(createMessage(sb.toString()));
  }

  public void printUsage() {
    // nothing to do
  }

  public char[] passwordPrompt(String format, Object... args) {
    // nothing to do -- return null
    return null;
  }

  public String prompt(String format, Object... args) {
    // nothing to do -- return null
    return null;
  }

  public void exit() {
    // nothing to do
  }

  public void addExitCallback(OpalShellExitCallback callback) {
    // nothing to do
  }

  //
  // Runnable Methods
  //

  public void run() {
    startTime = getCurrentTime();
    command.execute();
    endTime = getCurrentTime();
  }

  //
  // Methods
  //

  public Command<?> getCommand() {
    return command;
  }

  public void setCommand(Command<?> command) {
    this.command = command;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Date getSubmitTime() {
    return submitTime;
  }

  public void setSubmitTime(Date submitTime) {
    this.submitTime = submitTime;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public List<Message> getMessages() {
    return Collections.unmodifiableList(messages);
  }

  protected Date getCurrentTime() {
    return new Date();
  }

  protected Message createMessage(String msg) {
    return Message.newBuilder().setMsg(msg).build();
  }
}
