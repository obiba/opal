/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.shell.domain;

import com.google.gson.reflect.TypeToken;
import jakarta.persistence.*;
import org.obiba.opal.core.domain.converter.EnumNameConverter;
import org.obiba.opal.core.domain.converter.JsonAttributeConverter;
import org.obiba.opal.web.model.Commands.CommandStateDto.Status;
import org.springframework.data.domain.Persistable;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

/**
 * What is kept of a task once the server stops: its state, not its command. A command holds the services it was given
 * and cannot be rebuilt, so a task read back from here is shown in the history and never run again.
 * <p>
 * A separate class rather than {@code CommandJob} annotated in place, because a job is a live object - final fields,
 * the command it runs - and has no state of its own that JPA could fill.
 */
@Entity
@Table(name = "tasks",
    indexes = {
        @Index(name = "idx_tasks_owner", columnList = "owner"),
        @Index(name = "idx_tasks_project", columnList = "project")
    })
public class TaskState implements Persistable<Integer> {

  /**
   * Assigned by the task service, which keeps counting from the largest one stored.
   */
  @Id
  private Integer id;

  private String name;

  private String command;

  @Lob
  @Column(name = "command_args")
  private String commandArgs;

  private String owner;

  private String project;

  @Column(name = "r_cluster")
  private String rCluster;

  @Convert(converter = StatusConverter.class)
  private Status status;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "submit_time")
  private Date submitTime;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "start_time")
  private Date startTime;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "end_time")
  private Date endTime;

  @Lob
  @Convert(converter = TaskMessageListConverter.class)
  private List<TaskMessage> messages;

  /**
   * The id is assigned, so without this a save could not tell an insert from an update and would read the row first.
   */
  @Transient
  private boolean isNew;

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public boolean isNew() {
    return isNew;
  }

  public void setNew(boolean isNew) {
    this.isNew = isNew;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getCommandArgs() {
    return commandArgs;
  }

  public void setCommandArgs(String commandArgs) {
    this.commandArgs = commandArgs;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public String getRCluster() {
    return rCluster;
  }

  public void setRCluster(String rCluster) {
    this.rCluster = rCluster;
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

  public List<TaskMessage> getMessages() {
    return messages;
  }

  public void setMessages(List<TaskMessage> messages) {
    this.messages = messages;
  }

  /**
   * A task message, stored as plain JSON rather than as the protobuf message the job holds.
   */
  public static class TaskMessage {

    private String msg;

    private long timestamp;

    public TaskMessage() {
    }

    public TaskMessage(String msg, long timestamp) {
      this.msg = msg;
      this.timestamp = timestamp;
    }

    public String getMsg() {
      return msg;
    }

    public long getTimestamp() {
      return timestamp;
    }
  }

  @Converter
  public static class StatusConverter extends EnumNameConverter<Status> {

    public StatusConverter() {
      super(Status.class);
    }
  }

  @Converter
  public static class TaskMessageListConverter extends JsonAttributeConverter<List<TaskMessage>> {

    private static final Type TYPE = new TypeToken<List<TaskMessage>>() {
    }.getType();

    @Override
    protected Type getType() {
      return TYPE;
    }
  }
}
