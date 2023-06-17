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

import java.util.Set;

import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.CommandUsage;

/**
 * A registry of commands made available to a {@code OpalShell}. Consider extending {@code AbstractCommandRegistry}
 * instead of implementing this interface directly.
 */
public interface CommandRegistry {

  /**
   * Returns true if this registry has a command with the specified name.
   *
   * @param name
   * @return
   */
  boolean hasCommand(String name);

  /**
   * Returns the set of available command names.
   *
   * @return
   */
  Set<String> getAvailableCommands();

  /**
   * Retrieves the CommandUsage annotation for a command name.
   *
   * @param commandName The command name.
   */
  CommandUsage getCommandUsage(String commandName);

  /**
   * @param commandName the name of the command
   * @return the Class annotated with {@code CommandLineInterface} that defines the command's options
   */
  Class<?> getOptionsClass(String commandName);

  /**
   * Returns a new instance of the specified command using the specified arguments.
   *
   * @param name
   * @return
   */
  <T> Command<T> newCommand(String name);

}
