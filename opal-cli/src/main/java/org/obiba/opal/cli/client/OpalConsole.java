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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.util.LifecycleUtils;
import org.obiba.opal.cli.client.command.Command;
import org.obiba.opal.cli.client.command.DecryptCommand;
import org.obiba.opal.cli.client.command.ExportCommand;
import org.obiba.opal.cli.client.command.HelpCommand;
import org.obiba.opal.cli.client.command.ImportCommand;
import org.obiba.opal.cli.client.command.KeyCommand;
import org.obiba.opal.cli.client.command.ListCommand;
import org.obiba.opal.cli.client.command.PublicCommand;
import org.obiba.opal.cli.client.command.QuitCommand;
import org.obiba.opal.cli.client.command.ShowCommand;
import org.obiba.opal.cli.client.command.UpgradeCommand;
import org.obiba.opal.cli.client.command.UserAuthentication;
import org.obiba.opal.cli.client.command.VersionCommand;
import org.obiba.opal.cli.client.command.options.AuthenticationOptions;
import org.obiba.opal.cli.client.command.options.DecryptCommandOptions;
import org.obiba.opal.cli.client.command.options.ExportCommandOptions;
import org.obiba.opal.cli.client.command.options.HelpCommandOptions;
import org.obiba.opal.cli.client.command.options.HelpOption;
import org.obiba.opal.cli.client.command.options.ImportCommandOptions;
import org.obiba.opal.cli.client.command.options.KeyCommandOptions;
import org.obiba.opal.cli.client.command.options.ListCommandOptions;
import org.obiba.opal.cli.client.command.options.PublicCommandOptions;
import org.obiba.opal.cli.client.command.options.QuitCommandOptions;
import org.obiba.opal.cli.client.command.options.ShowCommandOptions;
import org.obiba.opal.cli.client.command.options.VersionCommandOptions;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.GenericXmlContextLoader;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException.ValidationError;

/**
 * 
 */
public class OpalConsole extends AbstractCliClient {

  public interface OpalConsoleOptions extends HelpOption, AuthenticationOptions {

    @Option(description = "Performs an Opal upgrade. Required after Opal installation.")
    public boolean isUpgrade();

  }

  private boolean quit = false;

  @Override
  public String getName() {
    return "opal";
  }

  public void quit() {
    this.quit = true;
  }

  protected void initAvailableCommands() {
    addAvailableCommand(HelpCommand.class, HelpCommandOptions.class);
    addAvailableCommand(QuitCommand.class, QuitCommandOptions.class);
    addAvailableCommand(ImportCommand.class, ImportCommandOptions.class);
    addAvailableCommand(KeyCommand.class, KeyCommandOptions.class);
    addAvailableCommand(PublicCommand.class, PublicCommandOptions.class);
    addAvailableCommand(DecryptCommand.class, DecryptCommandOptions.class);
    addAvailableCommand(VersionCommand.class, VersionCommandOptions.class);
    addAvailableCommand(ShowCommand.class, ShowCommandOptions.class);
    addAvailableCommand(ExportCommand.class, ExportCommandOptions.class);
    addAvailableCommand(ListCommand.class, ListCommandOptions.class);
  }

  private OpalConsole(OpalConsoleOptions options) {
    super();

    new UserAuthentication(options).authenticate();
    try {

      if(options.isUpgrade()) {
        // Perform an upgrade
        new UpgradeCommand().execute();
      } else {
        GenericXmlContextLoader ctxLoader = new GenericXmlContextLoader();
        ConfigurableApplicationContext cac = ctxLoader.loadContext("classpath:/spring/opal-cli/context.xml");
        try {
          // Start console
          prompt(cac);
        } finally {
          cac.close();
        }
      }

    } catch(Exception e) {
      System.err.println(e);
    } finally {
      SecurityUtils.getSubject().logout();
      // Destroy the security manager.
      LifecycleUtils.destroy(SecurityUtils.getSecurityManager());
    }

  }

  private void prompt(ConfigurableApplicationContext cac) {
    System.console().printf("Welcome to opal.\nType help to get a list of available commands.\n");
    while(quit == false) {
      String cmdline = System.console().readLine("%s@opal> ", SecurityUtils.getSubject().getPrincipal());
      if(cmdline == null) {
        break;
      }

      if(cmdline.trim().length() > 0) {
        String args[] = parseArguments(cmdline.trim());
        String commandName = args[0];

        if(hasCommand(commandName)) {
          args = Arrays.copyOfRange(args, 1, args.length);
          try {
            Command<?> command = newCommand(commandName, args);
            cac.getAutowireCapableBeanFactory().autowireBeanProperties(command, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
            command.execute();
          } catch(ArgumentValidationException e) {
            // Print some help to the user then fallback to prompt.
            printHelp(commandName, e);
          } catch(RuntimeException e) {
            System.console().printf("Error executing command '%s'.\n", commandName);
            if(e.getMessage() != null) System.console().printf("Error message: %s.\n", e.getMessage());
            System.console().printf("See log file for error details.\n");
            e.printStackTrace(System.err);
          }
        } else {
          System.console().printf("Unknown command '%s'. Type help to get help.\n", commandName);
        }
      }
    }
  }

  private void printHelp(String commandName, ArgumentValidationException e) {
    boolean helpRequested = false;
    for(ValidationError error : e.getValidationErrors()) {
      if(error.getErrorType() == ValidationError.ErrorType.HelpRequested) {
        helpRequested = true;
        System.console().printf("%s\n\n", getCommandDescriptionAndSyntax(commandName));
      }
      System.console().printf("%s\n", error);
    }
    if(helpRequested == false) {
      System.console().printf("Type '%s --help' for command usage.\n", commandName);
    }
  }

  private String getCommandDescriptionAndSyntax(String commandName) {
    String description = getCommandDescription(commandName);
    String syntax = getCommandSyntax(commandName);

    StringBuilder sb = new StringBuilder();
    if(description.length() != 0) {
      sb.append(description);
    }
    if(syntax.length() != 0) {
      sb.append("\n\n");
      sb.append(syntax);
    }

    return sb.toString();
  }

  public static void main(String[] args) {
    try {
      new OpalConsole(CliFactory.parseArguments(OpalConsoleOptions.class, args));
    } catch(ArgumentValidationException e) {
      System.console().printf("%s\n", e.getMessage());
    }
  }

  private static final String WHITESPACE_AND_QUOTE = " \t\r\n\"";

  private static final String QUOTE_ONLY = "\"";

  /**
   * Parses array of arguments using spaces as delimiters. Quoted strings (including spaces) are considered a single
   * argument. For example the command line:<br>{@code export --destination=opal onyx.Participants "onyx.Instrument Logs"}<br/>
   * would yield the array:<br/>
   * a[0] = export<br/>
   * a[1] = --destination=opal<br/>
   * a[2] = onyx.Participants<br/>
   * a[3] = onyx.Instrument Logs<br/>
   * @param string A command line of arguments to be parsed.
   * @return An array of arguments.
   */
  public static String[] parseArguments(String commandLine) {
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
