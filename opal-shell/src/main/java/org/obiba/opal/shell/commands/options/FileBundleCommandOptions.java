package org.obiba.opal.shell.commands.options;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

@CommandLineInterface(application = "file-bundle")
public interface FileBundleCommandOptions extends HelpOption {
  @Option(shortName = "z", description = "The file or folder path to bundle to a zip archive.")
  String getPath();
  @Option(shortName = "p", description = "The password to use to encrypt the file bundle.")
  String getPassword();
  boolean isPassword();
}
