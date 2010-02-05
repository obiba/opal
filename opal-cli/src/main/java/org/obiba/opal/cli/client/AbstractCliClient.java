/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.opal.cli.client.command.Command;
import org.obiba.opal.cli.client.command.CommandUsage;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;

/**
 * Base class for CLI clients.
 */
public abstract class AbstractCliClient implements CliClient {
  //
  // Instance Variables
  //

  @SuppressWarnings("unchecked")
  private Map<String, Class> commandMap;

  @SuppressWarnings("unchecked")
  private Map<String, Class> optionsMap;

  //
  // Constructors
  //

  @SuppressWarnings("unchecked")
  public AbstractCliClient() {

    // Tell Carol not to initialize its CMI component. This helps us minimize dependencies brought in by JOTM.
    // See: http://wiki.obiba.org/confluence/display/CAG/Technical+Requirements for details.
    System.setProperty("cmi.disabled", "true");

    commandMap = new HashMap<String, Class>();
    optionsMap = new HashMap<String, Class>();

    initAvailableCommands();
  }

  //
  // CliClient Methods
  //

  public abstract String getName();

  public void printUsage() {
    StringBuffer sb = new StringBuffer();

    sb.append("Usage:");
    sb.append("\n  ");
    sb.append("<command> <options> <arguments>");
    sb.append("\n\n");

    sb.append("Commands:\n");

    for(String command : availableCommands()) {
      sb.append("  ");
      sb.append(command);
      sb.append("\n");
    }

    sb.append("\n");
    sb.append("For help on a specific command, type:");
    sb.append("\n  ");
    sb.append("<command> --help");

    System.err.println(sb.toString());
  }

  public List<String> availableCommands() {
    return new ArrayList<String>(commandMap.keySet());
  }

  public boolean hasCommand(String commandName) {
    return commandMap.containsKey(commandName);
  }

  //
  // Methods
  //

  /**
   * Subclasses must implement this method to configure the available commands.
   * 
   * The implementation should call the <code>addAvailableCommand</code> method one or more times, according to the
   * number of commands.
   */
  protected abstract void initAvailableCommands();

  /**
   * Adds the specified command to the client's command set.
   * 
   * @param commandClass command class
   */
  protected <T> void addAvailableCommand(Class<? extends Command<T>> commandClass, Class<T> optionsClass) {
    if(optionsClass != null) {
      Annotation annotation = optionsClass.getAnnotation(CommandLineInterface.class);

      if(annotation != null && annotation instanceof CommandLineInterface) {
        CommandLineInterface cliAnnotation = (CommandLineInterface) annotation;
        commandMap.put(cliAnnotation.application(), commandClass);
        optionsMap.put(cliAnnotation.application(), optionsClass);
      }
    }
  }

  /**
   * Retrieves the command usage description.
   * 
   * @param commandName The command name.
   */
  protected String getCommandUsageDescription(String commandName) {
    Annotation commandUsage = commandMap.get(commandName).getAnnotation(CommandUsage.class);
    String commandUsageDescription = "";
    if(commandUsage != null) {
      commandUsageDescription = ((CommandUsage) commandUsage).description();
    }
    return commandUsageDescription;
  }

  /**
   * Parses the specified command line and returns the corresponding <code>Command</code> object.
   * 
   * @param cmdline command line
   * @return command object
   * @throws IllegalArgumentException if the command line specifies an invalid command
   * @throws ArgumentValidationException if the command line specifies invalid command options
   */
  protected <T> Command<T> parseCommand(String[] cmdline) throws IllegalArgumentException, ArgumentValidationException {
    if(cmdline.length == 0) {
      throw new IllegalArgumentException("No command");
    }

    String commandName = cmdline[0];
    String[] commandArgs = new String[cmdline.length - 1];
    System.arraycopy(cmdline, 1, commandArgs, 0, commandArgs.length);

    return newCommand(commandName, commandArgs);
  }

  @SuppressWarnings("unchecked")
  protected <T> Command<T> newCommand(String commandName, String[] commandArgs) throws ArgumentValidationException {
    Command<T> command = null;
    Class commandClass = commandMap.get(commandName);
    if(commandClass == null) {
      throw new IllegalArgumentException("Command not found (" + commandName + ")");
    }

    try {
      // Create the command object.
      command = (Command<T>) commandClass.newInstance();
      command.setClient(this);

      // Create the options object.
      Class optionsClass = optionsMap.get(commandName);
      if(optionsClass != null) {
        T options = (T) CliFactory.parseArguments(optionsClass, commandArgs);

        // Set the command's options.
        command.setOptions(options);
      } else {
        throw new IllegalStateException("No option class for command class " + commandClass.getName());
      }
    } catch(ArgumentValidationException e) {
      throw e;
    } catch(Exception e) {
      throw new IllegalArgumentException(e.getMessage());
    }

    return command;
  }
}