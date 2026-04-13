package org.obiba.opal.shell.commands.options;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

import java.util.List;

@CommandLineInterface(application = "file-bundle")
public interface FileBundleCommandOptions extends HelpOption {
  @Option(shortName = "z", description = "The file or folder paths to bundle to a zip archive.")
  List<String> getPaths();
  boolean isPaths();
  @Option(shortName = "p", description = "The password to use to encrypt the file bundle.")
  String getPassword();
  boolean isPassword();
}
