package org.obiba.opal.shell;

import java.util.Set;

import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.CommandUsage;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;

/**
 * A registry of commands made available to a {@code OpalShell}
 */
public interface CommandRegistry {

  boolean hasCommand(String name);

  Set<String> getAvailableCommands();

  /**
   * Retrieves the CommandUsage annotation for a command name.
   * 
   * @param commandName The command name.
   */
  CommandUsage getCommandUsage(String name);

  Command<?> newCommand(String name, String[] arguments) throws ArgumentValidationException;

}
