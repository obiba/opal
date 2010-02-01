package org.obiba.opal.cli.client.command.options;

import java.io.File;
import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * This interface declares the options that may be used with the "key" command.
 */
@CommandLineInterface(application = "key")
public interface KeyCommandOptions extends GlobalOptions {

  @Option(description = "Specify the alias name for the keystore entry.")
  public String getAlias();

  @Option(description = "Delete a key pair from the keystore. ")
  public boolean isDelete();

  @Option(longName = "algo", description = "Specify the algorithm.")
  public String getAlgorithm();

  public boolean isAlgorithm();

  @Option(description = "Specify the key size.")
  public int getSize();

  public boolean isSize();

  @Option(description = "Provides the private key file.")
  public List<File> getPrivate();

  public boolean isPrivate();

  @Option(description = "Provides the certified public key file, if omitted the user is prompt for creating one.")
  public List<File> getCertificate();

  public boolean isCertificate();
}
