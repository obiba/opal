package org.obiba.opal.shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.CommandUsage;

import com.google.common.collect.Sets;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException.ValidationError;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

/**
 * Implements {@code OpalShell} but does not specify how user interactions are implemented. Extending classes must
 * implement three methods {@code printf(String, Object...)}, {@code passwordPrompt(String, Object...)} and
 * {@code prompt(String, Object...)}
 */
public abstract class AbstractOpalShell implements OpalShell {

  private final CommandRegistry commandRegistry;

  private final List<OpalShellExitCallback> exitCallbacks = new ArrayList<>();

  private boolean quit = false;

  public AbstractOpalShell(CommandRegistry registry) {
    commandRegistry = registry;
  }

  @Override
  public void progress(String message, long current, long end, int percent) {
    printf("%s %d%% (%d/%d)", message, percent, current, end);
  }

  @Override
  public void run() {
    printf("Welcome to opal.\nType help to get a list of available commands.\n");
    while(!quit) {
      String cmdline = prompt("%s@opal> ", SecurityUtils.getSubject().getPrincipal());
      if(cmdline == null) {
        break;
      }

      if(cmdline.trim().length() > 0) {
        String args[] = CommandLines.parseArguments(cmdline.trim());
        String commandName = args[0];

        if(commandRegistry.hasCommand(commandName)) {
          executeCommand(commandName, args);
        } else {
          printf("Unknown command '%s'. Type help to get help.\n", commandName);
        }
      }
    }

  }

  @Override
  public void exit() {
    quit = true;
    for(OpalShellExitCallback callback : exitCallbacks) {
      callback.onExit();
    }
  }

  @Override
  public void addExitCallback(OpalShellExitCallback callback) {
    exitCallbacks.add(callback);
  }

  @Override
  public void printUsage() {

    int maxSize = Integer.MIN_VALUE;
    for(String command : commandRegistry.getAvailableCommands()) {
      maxSize = Math.max(maxSize, command.length() + 2);
    }
    printf("Usage:\n  <command> <options> <arguments>\n\nCommands:\n");
    // Create a TreeSet so that the output is sorted on command name
    for(String command : Sets.newTreeSet(commandRegistry.getAvailableCommands())) {
      // %-<maxSize>s: constantly print <maxSize> characters, left-justified
      printf("  %-" + maxSize + "s%s\n", command, commandRegistry.getCommandUsage(command).description());
    }
    printf("\nFor help on a specific command, type:\n<command> --help\n");
  }

  @Override
  public abstract void printf(String format, Object... args);

  @Override
  public abstract char[] passwordPrompt(String format, Object... args);

  @Override
  public abstract String prompt(String format, Object... args);

  private void executeCommand(String commandName, String... args) {
    String[] commandArgs = Arrays.copyOfRange(args, 1, args.length); // omit args[0], the command name
    try {
      // Create the options object.
      Class<?> optionsClass = commandRegistry.getOptionsClass(commandName);
      Object options = CliFactory.parseArguments(optionsClass, commandArgs);
      Command<Object> command = commandRegistry.newCommand(commandName);
      command.setOptions(options);
      command.setShell(this);
      command.execute();
    } catch(ArgumentValidationException e) {
      // Print some help to the user then fallback to prompt.
      printHelp(commandName, e);
    } catch(RuntimeException e) {
      printf("Error executing command '%s'.\n", commandName);
      if(e.getMessage() != null) printf("Error message: %s.\n", e.getMessage());
      printf("See log file for error details.\n");
      // TODO: where to we output this?
      e.printStackTrace(System.err);
    }
  }

  private void printHelp(String commandName, ArgumentValidationException e) {
    boolean helpRequested = false;
    for(ValidationError error : e.getValidationErrors()) {
      if(error.getErrorType() == ValidationError.ErrorType.HelpRequested) {
        helpRequested = true;

        CommandUsage usage = commandRegistry.getCommandUsage(commandName);
        printf("%s\n\n", usage.description());
        if(usage.syntax().length() > 0) {
          printf("%s\n\n", usage.syntax());
        }
      }
      printf("%s\n", error);
    }
    if(!helpRequested) {
      printf("Type '%s --help' for command usage.\n", commandName);
    }
  }
}
