package org.obiba.opal.shell.commands.options;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * This interface declares the options that may be used with the "key" command.
 */
@CommandLineInterface(application = "keystore")
public interface KeyCommandOptions extends HelpOption {
  @Option(shortName = "u", description = "The functional unit.")
  public String getUnit();

  public boolean isUnit();

  @Option(shortName = "a", description = "The name of the key within the keystore.")
  public String getAlias();

  public boolean isAlias();

  @Option(shortName = "x", description = "The action to perform: list, create, delete, import or export.")
  public String getAction();

  @Option(shortName = "g", longName = "algo", description = "The algorithm for creating a key pair. RSA is recommended.")
  public String getAlgorithm();

  public boolean isAlgorithm();

  @Option(shortName = "s", description = "The key size for creating a key pair.")
  public int getSize();

  public boolean isSize();

  @Option(shortName = "p", description = "Provides the private key file.")
  public String getPrivate();

  public boolean isPrivate();

  @Option(shortName = "c", description = "When action is 'import', indicates the certificate file to import (if omitted, user is prompted to create one). When action is 'export', indicates the file to which the exported certficate will be saved.")
  public String getCertificate();

  public boolean isCertificate();
}
