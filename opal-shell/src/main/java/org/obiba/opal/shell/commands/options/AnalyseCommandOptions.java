package org.obiba.opal.shell.commands.options;

import org.json.JSONObject;
import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

import java.util.List;

@CommandLineInterface(application = "analyse")
public interface AnalyseCommandOptions extends HelpOption {

  @Option(shortName = "p", description = "The source project in which table variables are to be analysed.")
  String getProject();

  @Option(shortName = "a", description = "List of analyse options providing table or variable names in addition to analysis name, plugin, template and routine parameters.")
  List<AnalyseOptions> getAnalyses();

  interface AnalyseOptions {
    String getTable();
    String getVariables();
    String getName();
    String getPlugin();
    String getTemplate();
    JSONObject getParams();
  }
}
