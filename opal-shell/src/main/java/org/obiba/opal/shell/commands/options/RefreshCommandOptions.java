package org.obiba.opal.shell.commands.options;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

@CommandLineInterface(application = "refresh")
public interface RefreshCommandOptions extends HelpOption {

  @Option(longName = "project", shortName = "p", description = "The project for which the underlying datasource will be refreshed.")
  String getProject();
}
