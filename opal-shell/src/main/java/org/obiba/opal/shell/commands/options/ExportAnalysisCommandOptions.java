package org.obiba.opal.shell.commands.options;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

import java.util.List;

@CommandLineInterface(application = "export-analysis")
public interface ExportAnalysisCommandOptions extends HelpOption {
  @Option(shortName = "p", description = "The project for which analysis data is to be exported.")
  String getProject();


  @Option(shortName = "a", description = "List of tables names for which analysis data is to be exported.")
  List<String> getTables();

}
