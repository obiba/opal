package org.obiba.opal.shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.shiro.SecurityUtils;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.CommandUsage;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException.ValidationError;

public abstract class AbstractOpalShell implements OpalShell {

  private static final String WHITESPACE_AND_QUOTE = " \t\r\n\"";

  private static final String QUOTE_ONLY = "\"";

  private final CommandRegistry commandRegistry;

  private final List<OpalShellExitCallback> exitCallbacks = new ArrayList<OpalShellExitCallback>();

  private boolean quit = false;

  public AbstractOpalShell(CommandRegistry registry) {
    this.commandRegistry = registry;
  }

  public void run() {
    printf("Welcome to opal.\nType help to get a list of available commands.\n");
    while(quit == false) {
      String cmdline = prompt("%s@opal> ", SecurityUtils.getSubject().getPrincipal());
      if(cmdline == null) {
        break;
      }

      if(cmdline.trim().length() > 0) {
        String args[] = parseArguments(cmdline.trim());
        String commandName = args[0];

        if(commandRegistry.hasCommand(commandName)) {
          args = Arrays.copyOfRange(args, 1, args.length);
          try {
            Command<?> command = commandRegistry.newCommand(commandName, args);
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
        } else {
          printf("Unknown command '%s'. Type help to get help.\n", commandName);
        }
      }
    }

  }

  public void exit() {
    quit = true;
    for(OpalShellExitCallback callback : exitCallbacks) {
      callback.onExit();
    }
  }

  public void addExitCallback(OpalShellExitCallback callback) {
    exitCallbacks.add(callback);
  }

  public void printUsage() {

    int maxSize = Integer.MIN_VALUE;
    for(String command : this.commandRegistry.getAvailableCommands()) {
      maxSize = Math.max(maxSize, command.length() + 2);
    }
    printf("Usage:\n  <command> <options> <arguments>\n\nCommands:\n");
    for(String command : this.commandRegistry.getAvailableCommands()) {
      // %-<maxSize>s: constantly print <maxSize> characters, left-justified
      printf("  %-" + maxSize + "s%s\n", command, this.commandRegistry.getCommandUsage(command).description());
    }
    printf("\nFor help on a specific command, type:\n<command> --help\n");
  }

  public abstract void printf(String format, Object... args);

  public abstract char[] passwordPrompt(String format, Object... args);

  public abstract String prompt(String format, Object... args);

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
    if(helpRequested == false) {
      printf("Type '%s --help' for command usage.\n", commandName);
    }
  }

  /**
   * Parses array of arguments using spaces as delimiters. Quoted strings (including spaces) are considered a single
   * argument. For example the command line:<br>{@code export --destination=opal onyx.Participants "onyx.Instrument Logs"}<br/>
   * would yield the array:<br/>
   * a[0] = export<br/>
   * a[1] = --destination=opal<br/>
   * a[2] = onyx.Participants<br/>
   * a[3] = onyx.Instrument Logs<br/>
   * 
   * @param string A command line of arguments to be parsed.
   * @return An array of arguments.
   */
  private String[] parseArguments(String commandLine) {
    List<String> arguments = new ArrayList<String>();
    String deliminator = WHITESPACE_AND_QUOTE;
    StringTokenizer parser = new StringTokenizer(commandLine, deliminator, true);

    String token = null;
    while(parser.hasMoreTokens()) {
      token = parser.nextToken(deliminator);
      if(!token.equals(QUOTE_ONLY)) {
        if(!token.trim().equals("")) {
          arguments.add(token);
        }
      } else {
        if(deliminator.equals(WHITESPACE_AND_QUOTE)) {
          deliminator = QUOTE_ONLY;
        } else {
          deliminator = WHITESPACE_AND_QUOTE;
        }
      }
    }
    return arguments.toArray(new String[0]);
  }
}
