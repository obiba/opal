package org.obiba.opal.shell.commands.options;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * This interface declares the options that may be used with the report command.
 */
@CommandLineInterface(application = "report")
public interface ReportCommandOptions extends HelpOption {

  @Option(shortName = "n", description = "The report template name.")
  String getName();

}
