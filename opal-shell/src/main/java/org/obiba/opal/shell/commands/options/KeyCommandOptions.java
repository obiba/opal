package org.obiba.opal.shell.commands.options;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * This interface declares the options that may be used with the "key" command.
 */
@CommandLineInterface(application = "keystore")
public interface KeyCommandOptions extends HelpOption {
  @Option(shortName = "u", description = "The functional unit.")
  String getUnit();

  boolean isUnit();

  @Option(shortName = "a", description = "The name of the key within the keystore.")
  String getAlias();

  boolean isAlias();

  @Option(shortName = "x", description = "The action to perform: list, create, delete, import or export.")
  String getAction();

  @Option(shortName = "g", longName = "algo",
      description = "The algorithm for creating a key pair. RSA is recommended.")
  String getAlgorithm();

  boolean isAlgorithm();

  @Option(shortName = "s", description = "The key size for creating a key pair.")
  int getSize();

  boolean isSize();

  @Option(shortName = "p", description = "Provides the private key file.")
  String getPrivate();

  boolean isPrivate();

  @Option(shortName = "c",
      description = "When action is 'import', indicates the certificate file to import (if omitted, user is prompted to create one). When action is 'export', indicates the file to which the exported certficate will be saved.")
  String getCertificate();

  boolean isCertificate();
}
