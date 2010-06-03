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
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
  public void testImportData() {
    // Setup
    Long jobId = 1l;
    CommandRegistry mockCommandRegistry = createMockCommandRegistry();
    ImportCommand importCommand = createImportCommand();
    expect(mockCommandRegistry.<ImportCommandOptions> newCommand(importCommand.getName())).andReturn(importCommand).atLeastOnce();

    CommandJobService mockCommandJobService = createMockCommandJobService(createEmptyCommandJobList());
    expect(mockCommandJobService.launchCommand(eqCommandJob(createCommandJob(importCommand, null)))).andReturn(jobId).atLeastOnce();

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
    Long jobId = 1l;
    CommandRegistry mockCommandRegistry = createMockCommandRegistry();
    CopyCommand copyCommand = createCopyCommand();
    expect(mockCommandRegistry.<CopyCommandOptions> newCommand(copyCommand.getName())).andReturn(copyCommand).atLeastOnce();

    CommandJobService mockCommandJobService = createMockCommandJobService(createEmptyCommandJobList());
    expect(mockCommandJobService.launchCommand(eqCommandJob(createCommandJob(copyCommand, null)))).andReturn(jobId).atLeastOnce();

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

    history.add(0, createCommandJob(createImportCommand(), createTimestamp(2010, Calendar.JANUARY, 1, 12, 0)));
    history.add(0, createCommandJob(createCopyCommand(), createTimestamp(2010, Calendar.JANUARY, 1, 12, 10)));

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

  private CommandJob createCommandJob(Command<?> command, Date submitTime) {
    CommandJob commandJob = new CommandJob();

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
