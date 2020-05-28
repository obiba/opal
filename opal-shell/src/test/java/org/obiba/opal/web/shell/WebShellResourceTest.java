/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.shell;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Test;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.CopyCommand;
import org.obiba.opal.shell.commands.ImportCommand;
import org.obiba.opal.shell.commands.ReportCommand;
import org.obiba.opal.shell.commands.options.ReportCommandOptions;
import org.obiba.opal.shell.service.CommandJobService;
import org.obiba.opal.shell.service.NoSuchCommandJobException;
import org.obiba.opal.web.model.Commands.CommandStateDto;
import org.obiba.opal.web.model.Commands.CommandStateDto.Status;
import org.obiba.opal.web.model.Commands.ReportCommandOptionsDto;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit tests for {@link WebShellResource}.
 */
public class WebShellResourceTest {

  @Test
  public void testGetCommands() {
    testGetCommands(createNonEmptyCommandJobList());
  }

  @Test
  public void testGetCommands_ReturnsEmptyListWhenThereIsNoHistory() {
    testGetCommands(createEmptyCommandJobList());
  }

  @Test
  public void testGetCommand_ReturnsResponseContainingCommandStateDtoOfSpecifiedJob() {
    // Setup
    Integer jobId = 1;
    CommandJob job = createCommandJob(jobId, createImportCommand(), new Date(1l));
    CommandJobService mockCommandJobService = createMock(CommandJobService.class);
    expect(mockCommandJobService.getCommand(jobId)).andReturn(job).atLeastOnce();

    WebShellResource sut = new WebShellResource();
    sut.setCommandJobService(mockCommandJobService);

    replay(mockCommandJobService);

    // Exercise
    Response response = sut.getCommand(jobId);

    // Verify mocks
    verify(mockCommandJobService);

    // Verify that the HTTP response code was OK (200) and that the body contains
    // the CommandStateDto of the specified task.
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    assertThat(response.getEntity()).isNotNull();
    assertThat(containsDtoForJob(response, job)).isTrue();
  }

  @Test
  public void testGetCommand_ReturnsNotFoundResponseIfJobDoesNotExist() {
    // Setup
    Integer bogusJobId = 1;
    CommandJobService mockCommandJobService = createMockCommandJobService();
    expect(mockCommandJobService.getCommand(bogusJobId)).andReturn(null).atLeastOnce();

    WebShellResource sut = new WebShellResource();
    sut.setCommandJobService(mockCommandJobService);

    replay(mockCommandJobService);

    // Exercise
    Response response = sut.getCommand(bogusJobId);

    // Verify mocks
    verify(mockCommandJobService);

    // Verify that the HTTP response code was NOT FOUND (404).
    assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
  }

  @Test
  public void testGetCommandStatus_ReturnsResponseContainingStatusSpecifiedJob() {
    // Setup
    Integer jobId = 1;
    CommandJob job = createCommandJob(jobId, createImportCommand(), new Date(1l));
    CommandJobService mockCommandJobService = createMock(CommandJobService.class);
    expect(mockCommandJobService.getCommand(jobId)).andReturn(job).atLeastOnce();

    WebShellResource sut = new WebShellResource();
    sut.setCommandJobService(mockCommandJobService);

    replay(mockCommandJobService);

    // Exercise
    Response response = sut.getCommandStatus(jobId);

    // Verify mocks
    verify(mockCommandJobService);

    // Verify that the HTTP response code was OK (200) and that the body contains
    // the status of the specified task.
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    assertThat(response.getEntity()).isNotNull();
    assertThat(response.getEntity()).isEqualTo(job.getStatus().toString());
  }

  @Test
  public void testGetCommandStatus_ReturnsNotFoundResponseIfJobDoesNotExist() {
    // Setup
    Integer bogusJobId = 1;
    CommandJobService mockCommandJobService = createMockCommandJobService();
    expect(mockCommandJobService.getCommand(bogusJobId)).andReturn(null).atLeastOnce();

    WebShellResource sut = new WebShellResource();
    sut.setCommandJobService(mockCommandJobService);

    replay(mockCommandJobService);

    // Exercise
    Response response = sut.getCommandStatus(bogusJobId);

    // Verify mocks
    verify(mockCommandJobService);

    // Verify that the HTTP response code was NOT FOUND (404).
    assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
  }

  @Test
  public void testSetCommandStatus() {
    // Setup
    Integer jobId = 1;
    CommandJobService mockCommandJobService = createMock(CommandJobService.class);
    mockCommandJobService.cancelCommand(jobId);
    expectLastCall().atLeastOnce();

    WebShellResource sut = new WebShellResource();
    sut.setCommandJobService(mockCommandJobService);

    replay(mockCommandJobService);

    // Exercise
    Response response = sut.setCommandStatus(jobId, Status.CANCELED.toString());

    // Verify mocks
    verify(mockCommandJobService);

    // Verify that the HTTP response code was OK (200).
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
  }

  @Test
  public void testSetCommandStatus_ReturnsNotFoundResponseIfJobDoesNotExist() {
    // Setup
    Integer bogusJobId = 1;
    CommandJobService mockCommandJobService = createMockCommandJobService();
    mockCommandJobService.cancelCommand(bogusJobId);
    expectLastCall().andThrow(new NoSuchCommandJobException(bogusJobId));

    WebShellResource sut = new WebShellResource();
    sut.setCommandJobService(mockCommandJobService);

    replay(mockCommandJobService);

    // Exercise
    Response response = sut.setCommandStatus(bogusJobId, Status.CANCELED.toString());

    // Verify mocks
    verify(mockCommandJobService);

    // Verify that the HTTP response code was NOT FOUND (404).
    assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
  }

  @Test
  public void testSetCommandStatus_ReturnsBadRequestResponseIfJobIsNotCancellable() {
    // Setup
    Integer bogusJobId = 1;
    CommandJobService mockCommandJobService = createMockCommandJobService();
    mockCommandJobService.cancelCommand(bogusJobId);
    expectLastCall().andThrow(new IllegalStateException());

    WebShellResource sut = new WebShellResource();
    sut.setCommandJobService(mockCommandJobService);

    replay(mockCommandJobService);

    // Exercise
    Response response = sut.setCommandStatus(bogusJobId, Status.CANCELED.toString());

    // Verify mocks
    verify(mockCommandJobService);

    // Verify that the HTTP response code was BAD REQUEST (400).
    assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
  }

  @Test
  public void testSetCommandStatus_ReturnsBadRequestResponseForNewStatusOtherThanCanceled() {
    // Setup
    Integer jobId = 1;
    WebShellResource sut = new WebShellResource();

    // Exercise
    Response response = sut.setCommandStatus(jobId, Status.SUCCEEDED.toString());

    // Verify that the HTTP response code was BAD REQUEST (400).
    assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
  }

  @Test
  public void testDeleteCommand() {
    // Setup
    Integer jobId = 1;
    CommandJobService mockCommandJobService = createMock(CommandJobService.class);
    mockCommandJobService.deleteCommand(jobId);
    expectLastCall().atLeastOnce();

    WebShellResource sut = new WebShellResource();
    sut.setCommandJobService(mockCommandJobService);

    replay(mockCommandJobService);

    // Exercise
    Response response = sut.deleteCommand(jobId);

    // Verify mocks
    verify(mockCommandJobService);

    // Verify that the HTTP response code was OK (200).
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
  }

  @Test
  public void testDeleteCommand_ReturnsNotFoundResponseIfJobDoesNotExist() {
    // Setup
    Integer bogusJobId = 1;
    CommandJobService mockCommandJobService = createMockCommandJobService();
    mockCommandJobService.deleteCommand(bogusJobId);
    expectLastCall().andThrow(new NoSuchCommandJobException(bogusJobId));

    WebShellResource sut = new WebShellResource();
    sut.setCommandJobService(mockCommandJobService);

    replay(mockCommandJobService);

    // Exercise
    Response response = sut.deleteCommand(bogusJobId);

    // Verify mocks
    verify(mockCommandJobService);

    // Verify that the HTTP response code was NOT FOUND (404).
    assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
  }

  @Test
  public void testDeleteCommand_ReturnsBadRequestResponseIfJobIsNotDeletable() {
    // Setup
    Integer jobId = 1;
    CommandJobService mockCommandJobService = createMockCommandJobService();
    mockCommandJobService.deleteCommand(jobId);
    expectLastCall().andThrow(new IllegalStateException());

    WebShellResource sut = new WebShellResource();
    sut.setCommandJobService(mockCommandJobService);

    replay(mockCommandJobService);

    // Exercise
    Response response = sut.deleteCommand(jobId);

    // Verify mocks
    verify(mockCommandJobService);

    // Verify that the HTTP response code was BAD REQUEST (400).
    assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
  }

  @Test
  public void testDeleteCompletedCommands() {
    // Setup
    CommandJobService mockCommandJobService = createMockCommandJobService();
    mockCommandJobService.deleteCompletedCommands();
    expectLastCall().once();

    WebShellResource sut = new WebShellResource();
    sut.setCommandJobService(mockCommandJobService);

    replay(mockCommandJobService);

    // Exercise
    Response response = sut.deleteCompletedCommands();

    // Verify mocks
    verify(mockCommandJobService);

    // Verify that the HTTP response code was OK (200).
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
  }

  @Test
  public void testCreateReport() {
    // Setup
    Integer jobId = 1;
    CommandRegistry mockCommandRegistry = createMockCommandRegistry();
    ReportCommand reportCommand = createReportCommand();
    expect(mockCommandRegistry.<ReportCommandOptions>newCommand(reportCommand.getName())).andReturn(reportCommand)
        .atLeastOnce();

    CommandJobService mockCommandJobService = createMockCommandJobService();
    expect(mockCommandJobService.launchCommand(eqCommandJob(createCommandJob(jobId, reportCommand, null))))
        .andReturn(jobId).atLeastOnce();

    WebShellResource sut = new WebShellResource();
    sut.setCommandJobService(mockCommandJobService);
    sut.setCommandRegistry(mockCommandRegistry);

    replay(mockCommandRegistry, mockCommandJobService);

    // Exercise
    ReportCommandOptionsDto optionsDto = createReportCommandOptionsDto("test report", "project1");
    Response response = sut.createReport(optionsDto);

    // Verify mocks
    verify(mockCommandRegistry, mockCommandJobService);

    // Verify that the options in the dto were applied to the launched command
    ReportCommandOptions importOptions = reportCommand.getOptions();
    assertThat(optionsDto.getName()).isEqualTo(importOptions.getName());

    // Verify that the HTTP response code was CREATED (201) and that the "Location"
    // header was set to '/shell/command/{jobId}'.
    assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    assertThat(response.getMetadata().getFirst("Location").toString()).isEqualTo("/shell/command/" + jobId);
  }

  //
  // Private Methods
  //

  private void testGetCommands(List<CommandJob> commandJobList) {
    // Setup
    CommandJobService mockCommandJobService = createMockCommandJobService();
    expect(mockCommandJobService.getHistory()).andReturn(commandJobList).atLeastOnce();

    WebShellResource sut = new WebShellResource();
    sut.setCommandJobService(mockCommandJobService);

    replay(mockCommandJobService);

    // Exercise
    List<CommandStateDto> commandStateDtoList = sut.getCommands();

    // Verify behaviour
    verify(mockCommandJobService);

    // Verify state
    assertThat(commandStateDtoList).isNotNull();
    assertDtoListMatchesJobList(commandStateDtoList, commandJobList);
  }

  private void assertDtoListMatchesJobList(List<CommandStateDto> dtoList, List<CommandJob> jobList) {
    assertThat(jobList.size()).isEqualTo(dtoList.size());

    for(int i = 0; i < dtoList.size(); i++) {
      CommandStateDto dto = dtoList.get(i);
      CommandJob job = jobList.get(i);

      assertThat(job.getCommand().getName()).isEqualTo(dto.getCommand());
      assertThat(job.getOwner()).isEqualTo(dto.getOwner());
      assertThat(job.getStatus().toString()).isEqualTo(dto.getStatus());
    }
  }

  private CommandJobService createMockCommandJobService() {
    return createMock(CommandJobService.class);
  }

  private CommandRegistry createMockCommandRegistry() {
    return createMock(CommandRegistry.class);
  }

  private List<CommandJob> createNonEmptyCommandJobList() {
    List<CommandJob> history = new ArrayList<>();

    history.add(0, createCommandJob(1, createImportCommand(), createTimestamp(2010, Calendar.JANUARY, 1, 12, 0)));
    history.add(0, createCommandJob(2, createCopyCommand(), createTimestamp(2010, Calendar.JANUARY, 1, 12, 10)));

    return history;
  }

  private List<CommandJob> createEmptyCommandJobList() {
    return new ArrayList<>();
  }

  private ReportCommandOptionsDto createReportCommandOptionsDto(String name, String project) {
    return ReportCommandOptionsDto.newBuilder().setName(name).setProject(project).build();
  }

  private CommandJob createCommandJob(Integer id, Command<?> command, Date submitTime) {
    CommandJob commandJob = new CommandJob(command);

    commandJob.setId(id);
    commandJob.setOwner("someUser");
    commandJob.setStatus(Status.SUCCEEDED);

    if(submitTime != null) {
      commandJob.setSubmitTime(submitTime);
      commandJob.run();
    }

    return commandJob;
  }

  private ImportCommand createImportCommand() {

    return new ImportCommand() {
      @Override
      public String getName() {
        return "import";
      }

      @Override
      public String toString() {
        return "import args";
      }

      @Override
      public int execute() {
        return 0;
      }
    };
  }

  private ReportCommand createReportCommand() {

    return new ReportCommand() {
      @Override
      public String getName() {
        return "report";
      }

      @Override
      public String toString() {
        return "report args";
      }

      @Override
      public int execute() {
        return 0;
      }
    };
  }

  private CopyCommand createCopyCommand() {

    return new CopyCommand() {
      @Override
      public String getName() {
        return "copy";
      }

      @Override
      public String toString() {
        return "copy args";
      }

      @Override
      public int execute() {
        return 0;
      }

    };
  }

  private Date createTimestamp(int year, int month, int date, int hour, int minute) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(year, month, date, hour, minute);

    return calendar.getTime();
  }

  private boolean containsDtoForJob(Response response, CommandJob job) {
    Object entity = response.getEntity();

    if(entity != null) {
      if(entity instanceof CommandStateDto) {
        CommandStateDto dto = (CommandStateDto) entity;
        return dto.getId() == job.getId() && dto.getCommand().equals(job.getCommand().getName()) &&
            dto.getCommandArgs().equals(job.getCommand().toString()) && dto.getOwner().equals(job.getOwner()) &&
            dto.getStatus().equals(job.getStatus().toString());
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  //
  // Inner Classes
  //

  static class CommandJobMatcher implements IArgumentMatcher {

    private final CommandJob expected;

    CommandJobMatcher(CommandJob expected) {
      this.expected = expected;
    }

    @Override
    public boolean matches(Object actual) {
      if(actual instanceof CommandJob) {
        CommandJob actualJob = (CommandJob) actual;
        return actualJob.getCommand().getName().equals(expected.getCommand().getName()) &&
            actualJob.getCommand().toString().equals(expected.getCommand().toString());
      } else {
        return false;
      }
    }

    @Override
    public void appendTo(StringBuffer buffer) {
      buffer.append("eqCommandJob(");
      buffer.append(expected.getClass().getName());
      buffer.append(" with command \"");
      buffer.append(expected.getCommand().getName());
      buffer.append(" and toString \"");
      buffer.append(expected.getCommand());
      buffer.append("\")");
    }
  }

  static CommandJob eqCommandJob(CommandJob in) {
    EasyMock.reportMatcher(new CommandJobMatcher(in));
    return null;
  }
}
