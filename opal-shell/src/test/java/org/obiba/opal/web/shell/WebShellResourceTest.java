/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.shell;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
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
import org.obiba.opal.shell.commands.options.CopyCommandOptions;
import org.obiba.opal.shell.commands.options.ImportCommandOptions;
import org.obiba.opal.shell.service.CommandJobService;
import org.obiba.opal.shell.service.NoSuchCommandJobException;
import org.obiba.opal.web.model.Commands.CommandStateDto;
import org.obiba.opal.web.model.Commands.CopyCommandOptionsDto;
import org.obiba.opal.web.model.Commands.ImportCommandOptionsDto;
import org.obiba.opal.web.model.Commands.CommandStateDto.Status;

/**
 * Unit tests for {@link WebShellResource}.
 */
public class WebShellResourceTest {
  //
  // Test Methods
  //

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
    // the CommandStateDto of the specified job.
    assertNotNull(response);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    assertNotNull(response.getEntity());
    assertTrue(containsDtoForJob(response, job));
  }

  @Test
  public void testGetCommand_ReturnsNotFoundResponseIfJobDoesNotExist() {
    // Setup
    Integer bogusJobId = 1;
    CommandJobService mockCommandJobService = createMockCommandJobService(createEmptyCommandJobList());
    expect(mockCommandJobService.getCommand(bogusJobId)).andReturn(null).atLeastOnce();

    WebShellResource sut = new WebShellResource();
    sut.setCommandJobService(mockCommandJobService);

    replay(mockCommandJobService);

    // Exercise
    Response response = sut.getCommand(bogusJobId);

    // Verify mocks
    verify(mockCommandJobService);

    // Verify that the HTTP response code was NOT FOUND (404).
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
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
    // the status of the specified job.
    assertNotNull(response);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    assertNotNull(response.getEntity());
    assertEquals(job.getStatus().toString(), response.getEntity());
  }

  @Test
  public void testGetCommandStatus_ReturnsNotFoundResponseIfJobDoesNotExist() {
    // Setup
    Integer bogusJobId = 1;
    CommandJobService mockCommandJobService = createMockCommandJobService(createEmptyCommandJobList());
    expect(mockCommandJobService.getCommand(bogusJobId)).andReturn(null).atLeastOnce();

    WebShellResource sut = new WebShellResource();
    sut.setCommandJobService(mockCommandJobService);

    replay(mockCommandJobService);

    // Exercise
    Response response = sut.getCommandStatus(bogusJobId);

    // Verify mocks
    verify(mockCommandJobService);

    // Verify that the HTTP response code was NOT FOUND (404).
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
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
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void testSetCommandStatus_ReturnsNotFoundResponseIfJobDoesNotExist() {
    // Setup
    Integer bogusJobId = 1;
    CommandJobService mockCommandJobService = createMockCommandJobService(createEmptyCommandJobList());
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
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void testSetCommandStatus_ReturnsBadRequestResponseIfJobIsNotCancellable() {
    // Setup
    Integer bogusJobId = 1;
    CommandJobService mockCommandJobService = createMockCommandJobService(createEmptyCommandJobList());
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
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void testSetCommandStatus_ReturnsBadRequestResponseForNewStatusOtherThanCanceled() {
    // Setup
    Integer jobId = 1;
    WebShellResource sut = new WebShellResource();

    // Exercise
    Response response = sut.setCommandStatus(jobId, Status.SUCCEEDED.toString());

    // Verify that the HTTP response code was BAD REQUEST (400).
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
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
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }
  
  @Test
  public void testDeleteCommand_ReturnsNotFoundResponseIfJobDoesNotExist() {
    // Setup
    Integer bogusJobId = 1;
    CommandJobService mockCommandJobService = createMockCommandJobService(createEmptyCommandJobList());
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
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void testDeleteCommand_ReturnsBadRequestResponseIfJobIsNotDeletable() {
    // Setup
    Integer jobId = 1;
    CommandJobService mockCommandJobService = createMockCommandJobService(createEmptyCommandJobList());
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
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void testImportData() {
    // Setup
    Integer jobId = 1;
    CommandRegistry mockCommandRegistry = createMockCommandRegistry();
    ImportCommand importCommand = createImportCommand();
    expect(mockCommandRegistry.<ImportCommandOptions> newCommand(importCommand.getName())).andReturn(importCommand).atLeastOnce();

    CommandJobService mockCommandJobService = createMockCommandJobService(createEmptyCommandJobList());
    expect(mockCommandJobService.launchCommand(eqCommandJob(createCommandJob(jobId, importCommand, null)))).andReturn(jobId).atLeastOnce();

    WebShellResource sut = new WebShellResource();
    sut.setCommandRegistry(mockCommandRegistry);
    sut.setCommandJobService(mockCommandJobService);

    replay(mockCommandRegistry, mockCommandJobService);

    // Exercise
    ImportCommandOptionsDto optionsDto = createImportCommandOptionsDto("my-unit", "opal-data", null, "file1", "file2");
    Response response = sut.importData(optionsDto);

    // Verify mocks
    verify(mockCommandRegistry, mockCommandJobService);

    // Verify that the options in the dto were applied to the launched command
    ImportCommandOptions importOptions = importCommand.getOptions();
    assertEquals(optionsDto.getUnit(), importOptions.getUnit());
    assertEquals(optionsDto.getDestination(), importOptions.getDestination());
    assertEquals(optionsDto.getFilesCount(), importOptions.getFiles().size());
    for(int i = 0; i < optionsDto.getFilesCount(); i++) {
      assertEquals(optionsDto.getFiles(i), importOptions.getFiles().get(i));
    }

    // Verify that the HTTP response code was CREATED (201) and that the "Location"
    // header was set to '/shell/command/{jobId}'.
    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    assertEquals("/shell/command/" + jobId, response.getMetadata().getFirst("Location").toString());
  }

  @Test
  public void testCopyData() {
    // Setup
    Integer jobId = 1;
    CommandRegistry mockCommandRegistry = createMockCommandRegistry();
    CopyCommand copyCommand = createCopyCommand();
    expect(mockCommandRegistry.<CopyCommandOptions> newCommand(copyCommand.getName())).andReturn(copyCommand).atLeastOnce();

    CommandJobService mockCommandJobService = createMockCommandJobService(createEmptyCommandJobList());
    expect(mockCommandJobService.launchCommand(eqCommandJob(createCommandJob(jobId, copyCommand, null)))).andReturn(jobId).atLeastOnce();

    WebShellResource sut = new WebShellResource();
    sut.setCommandRegistry(mockCommandRegistry);
    sut.setCommandJobService(mockCommandJobService);

    replay(mockCommandRegistry, mockCommandJobService);

    // Exercise
    CopyCommandOptionsDto optionsDto = createCopyCommandOptionsDto("opal-data", "jdbc", null, null, null);
    Response response = sut.copyData(optionsDto);

    // Verify mocks
    verify(mockCommandRegistry, mockCommandJobService);

    // Verify that the options in the dto were applied to the launched command
    CopyCommandOptions copyOptions = copyCommand.getOptions();
    assertEquals(optionsDto.getSource(), copyOptions.getSource());
    assertEquals(optionsDto.getDestination(), copyOptions.getDestination());
    assertEquals(optionsDto.getTablesCount(), copyOptions.getTables().size());

    // Verify that the HTTP response code was CREATED (201) and that the "Location"
    // header was set to '/shell/command/{jobId}'.
    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    assertEquals("/shell/command/" + jobId, response.getMetadata().getFirst("Location").toString());
  }

  //
  // Private Methods
  //

  private void testGetCommands(List<CommandJob> commandJobList) {
    // Setup
    CommandJobService mockCommandJobService = createMockCommandJobService(commandJobList);
    expect(mockCommandJobService.getHistory()).andReturn(commandJobList).atLeastOnce();

    WebShellResource sut = new WebShellResource();
    sut.setCommandJobService(mockCommandJobService);

    replay(mockCommandJobService);

    // Exercise
    List<CommandStateDto> commandStateDtoList = sut.getCommands();

    // Verify behaviour
    verify(mockCommandJobService);

    // Verify state
    assertNotNull(commandStateDtoList);
    assertDtoListMatchesJobList(commandStateDtoList, commandJobList);
  }

  private void assertDtoListMatchesJobList(List<CommandStateDto> dtoList, List<CommandJob> jobList) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(CommandJob.DATE_FORMAT_PATTERN);

    assertEquals(jobList.size(), dtoList.size());

    for(int i = 0; i < dtoList.size(); i++) {
      CommandStateDto dto = dtoList.get(i);
      CommandJob job = jobList.get(i);

      assertEquals(job.getCommand().getName(), dto.getCommand());
      assertEquals(job.getOwner(), dto.getOwner());
      assertEquals(job.getStatus(), dto.getStatus());
      assertEquals(dateFormat.format(job.getStartTime()), dto.getStartTime());
      assertEquals(dateFormat.format(job.getEndTime()), dto.getEndTime());
    }
  }

  private CommandJobService createMockCommandJobService(List<CommandJob> history) {
    CommandJobService mockCommandJobService = createMock(CommandJobService.class);
    return mockCommandJobService;
  }

  private CommandRegistry createMockCommandRegistry() {
    CommandRegistry mockCommandRegistry = createMock(CommandRegistry.class);
    return mockCommandRegistry;
  }

  private List<CommandJob> createNonEmptyCommandJobList() {
    List<CommandJob> history = new ArrayList<CommandJob>();

    history.add(0, createCommandJob(1, createImportCommand(), createTimestamp(2010, Calendar.JANUARY, 1, 12, 0)));
    history.add(0, createCommandJob(2, createCopyCommand(), createTimestamp(2010, Calendar.JANUARY, 1, 12, 10)));

    return history;
  }

  private List<CommandJob> createEmptyCommandJobList() {
    return new ArrayList<CommandJob>();
  }

  private ImportCommandOptionsDto createImportCommandOptionsDto(String unit, String destination, String archive, String... files) {
    ImportCommandOptionsDto.Builder dtoBuilder = ImportCommandOptionsDto.newBuilder();

    dtoBuilder.setUnit(unit);
    dtoBuilder.setDestination(destination);

    if(archive != null) {
      dtoBuilder.setArchive(archive);
    }

    for(String file : files) {
      dtoBuilder.addFiles(file);
    }

    return dtoBuilder.build();
  }

  private CopyCommandOptionsDto createCopyCommandOptionsDto(String source, String destination, String out, String multiplex, String transform, String... tables) {
    CopyCommandOptionsDto.Builder dtoBuilder = CopyCommandOptionsDto.newBuilder();

    if(source != null) {
      dtoBuilder.setSource(source);
    }
    if(destination != null) {
      dtoBuilder.setDestination(destination);
    }
    if(out != null) {
      dtoBuilder.setOut(out);
    }
    if(multiplex != null) {
      dtoBuilder.setMultiplex(multiplex);
    }
    if(transform != null) {
      dtoBuilder.setTransform(transform);
    }

    for(String table : tables) {
      dtoBuilder.addTables(table);
    }

    return dtoBuilder.build();
  }

  private CommandJob createCommandJob(Integer id, Command<?> command, Date submitTime) {
    CommandJob commandJob = new CommandJob();

    commandJob.setId(id);
    commandJob.setCommand(command);
    commandJob.setOwner("someUser");
    commandJob.setStatus(Status.SUCCEEDED);

    if(submitTime != null) {
      commandJob.setSubmitTime(submitTime);
      commandJob.setStartTime(rollTimestamp(submitTime, Calendar.MINUTE, 1));
      commandJob.setEndTime(rollTimestamp(commandJob.getStartTime(), Calendar.MINUTE, 5));
    }

    return commandJob;
  }

  private ImportCommand createImportCommand() {
    ImportCommand command = new ImportCommand() {
      @Override
      public String getName() {
        return "import";
      }

      @Override
      public String toString() {
        return "import args";
      }
    };

    return command;
  }

  private CopyCommand createCopyCommand() {
    CopyCommand command = new CopyCommand() {
      @Override
      public String getName() {
        return "copy";
      }

      @Override
      public String toString() {
        return "copy args";
      }
    };

    return command;
  }

  private Date createTimestamp(int year, int month, int date, int hour, int minute) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(year, month, date, hour, minute);

    return calendar.getTime();
  }

  private Date rollTimestamp(Date timestamp, int field, int amount) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(timestamp);
    calendar.add(field, amount);

    return calendar.getTime();
  }

  private boolean containsDtoForJob(Response response, CommandJob job) {
    Object entity = response.getEntity();

    if(entity != null) {
      if(entity instanceof CommandStateDto) {
        CommandStateDto dto = (CommandStateDto) entity;
        return (dto.getId() == job.getId() && dto.getCommand().equals(job.getCommand().getName()) && dto.getCommandArgs().equals(job.getCommand().toString()) && dto.getOwner().equals(job.getOwner()) && dto.getStatus().equals(job.getStatus()));
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

    private CommandJob expected;

    public CommandJobMatcher(CommandJob expected) {
      this.expected = expected;
    }

    public boolean matches(Object actual) {
      if(actual instanceof CommandJob) {
        CommandJob actualJob = (CommandJob) actual;
        return (actualJob.getCommand().getName().equals(expected.getCommand().getName()) && actualJob.getCommand().toString().equals(expected.getCommand().toString()));
      } else {
        return false;
      }
    }

    public void appendTo(StringBuffer buffer) {
      buffer.append("eqCommandJob(");
      buffer.append(expected.getClass().getName());
      buffer.append(" with command \"");
      buffer.append(expected.getCommand().getName());
      buffer.append(" and toString \"");
      buffer.append(expected.getCommand().toString());
      buffer.append("\")");
    }
  }

  static CommandJob eqCommandJob(CommandJob in) {
    EasyMock.reportMatcher(new CommandJobMatcher(in));
    return null;
  }
}
