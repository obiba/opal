package org.obiba.opal.shell.commands.options;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * This interface declares the options that may be used with the "public" command.
 */
@CommandLineInterface(application = "certificate")
public interface PublicCommandOptions extends HelpOption {

  @Option(shortName = "u", description = "The functional unit. Defaults to 'OpalInstance'.")
  public String getUnit();

  public boolean isUnit();

  @Option(shortName = "a", description = "The alias name of the encryption key pair.")
  public String getAlias();

  @Option(shortName = "o", description = "Export the certificate to the specified file, in pem format")
  public String getOut();

  public boolean isOut();

}
