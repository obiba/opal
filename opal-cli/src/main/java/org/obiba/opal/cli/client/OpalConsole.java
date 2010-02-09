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

import java.util.Arrays;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.util.LifecycleUtils;
import org.obiba.opal.cli.client.command.Command;
import org.obiba.opal.cli.client.command.DecryptCommand;
import org.obiba.opal.cli.client.command.ExportCommand;
import org.obiba.opal.cli.client.command.HelpCommand;
import org.obiba.opal.cli.client.command.ImportCommand;
import org.obiba.opal.cli.client.command.KeyCommand;
import org.obiba.opal.cli.client.command.PublicCommand;
import org.obiba.opal.cli.client.command.QuitCommand;
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
import org.obiba.opal.cli.client.command.options.PublicCommandOptions;
import org.obiba.opal.cli.client.command.options.QuitCommandOptions;
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
    addAvailableCommand(ExportCommand.class, ExportCommandOptions.class);
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
    while(quit == false) {
      String cmdline = System.console().readLine("%s@opal> ", SecurityUtils.getSubject().getPrincipal());
      if(cmdline == null) {
        break;
      }

      if(cmdline.trim().length() > 0) {
        String args[] = cmdline.trim().split("\\s");
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
            e.printStackTrace();
            System.err.println(e);
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
        System.console().printf(getCommandUsageDescription(commandName) + "\n\n");
      }
      System.err.println(error);
    }
    if(helpRequested == false) {
      System.console().printf("Type '%s --help' for command usage.\n", commandName);
    }
  }

  public static void main(String[] args) {
    try {
      new OpalConsole(CliFactory.parseArguments(OpalConsoleOptions.class, args));
    } catch(ArgumentValidationException e) {
      System.err.println(e.getMessage());
    }
  }
}
