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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.CommandUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.ImmutableSet;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;

/**
 * An abstract implementation of {@code CommandRegistry}. Extending classes should invoke the {@code
 * addAvailableCommand(Class, Class)} method with all commands that should be made available.
 */
public abstract class AbstractCommandRegistry implements CommandRegistry {
  //
  // Instance Variables
  //

  @SuppressWarnings("unchecked")
  private final Map<String, Class<? extends Command>> commandMap;

  @SuppressWarnings("unchecked")
  private final Map<String, Class> optionsMap;

  @Autowired
  private ApplicationContext ctx;

  //
  // Constructors
  //

  @SuppressWarnings("unchecked")
  public AbstractCommandRegistry() {
    commandMap = new HashMap<String, Class<? extends Command>>();
    optionsMap = new HashMap<String, Class>();
  }

  @Override
  public Set<String> getAvailableCommands() {
    return ImmutableSet.copyOf(commandMap.keySet());
  }

  @Override
  public boolean hasCommand(String commandName) {
    return commandMap.containsKey(commandName);
  }

  /**
   * Adds the specified command to the client's command set.
   *
   * @param commandClass command class
   */
  protected <T> void addAvailableCommand(Class<? extends Command<T>> commandClass, Class<T> optionsClass) {
    if(commandClass == null) throw new IllegalArgumentException("commandClass cannot be null");
    if(optionsClass == null) throw new IllegalArgumentException("optionClass cannot be null");
    if(commandClass.isAnnotationPresent(CommandUsage.class) == false) throw new IllegalArgumentException(
        "command class " + commandClass.getName() + " must be annotated with @CommandUsage");
    if(optionsClass.isAnnotationPresent(CommandLineInterface.class) == false) throw new IllegalArgumentException(
        "options class " + optionsClass.getName() + " must be annotated with @CommandLineInterface");

    CommandLineInterface annotation = optionsClass.getAnnotation(CommandLineInterface.class);
    commandMap.put(annotation.application(), commandClass);
    optionsMap.put(annotation.application(), optionsClass);
  }

  @Override
  public CommandUsage getCommandUsage(String commandName) {
    if(commandName == null) throw new IllegalArgumentException("commandName cannot be null");
    return commandMap.get(commandName).getAnnotation(CommandUsage.class);
  }

  @Override
  public Class<?> getOptionsClass(String commandName) {
    if(commandName == null) throw new IllegalArgumentException("commandName cannot be null");
    if(hasCommand(commandName) == false) throw new IllegalArgumentException("no such command " + commandName);
    return optionsMap.get(commandName);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Command<T> newCommand(String commandName) {
    Class<T> commandClass = (Class<T>) commandMap.get(commandName);
    if(commandClass == null) {
      throw new IllegalArgumentException("Command not found (" + commandName + ")");
    }

    try {
      // Create the command object.
      Command<T> command = (Command<T>) commandClass.newInstance();
      ctx.getAutowireCapableBeanFactory()
          .autowireBeanProperties(command, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
      return command;
    } catch(Exception e) {
      throw new IllegalArgumentException(e);
    }
  }
}