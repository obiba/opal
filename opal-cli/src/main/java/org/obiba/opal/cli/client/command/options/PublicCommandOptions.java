package org.obiba.opal.cli.client.command.options;

import java.io.File;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * This interface declares the options that may be used with the "public" command.
 */
@CommandLineInterface(application = "certificate")
public interface PublicCommandOptions extends HelpOption {

  @Option(shortName = "a", description = "The alias name of the encryption key pair.")
  public String getAlias();

  @Option(shortName = "o", description = "Export the certificate to the specified file, in pem format")
  public File getOut();

  public boolean isOut();

}
