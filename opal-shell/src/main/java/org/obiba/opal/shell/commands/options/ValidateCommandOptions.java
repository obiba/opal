package org.obiba.opal.shell.commands.options;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

@CommandLineInterface(application = "validate")
public interface ValidateCommandOptions  extends HelpOption {

    @Option(shortName = "d",
            description = "The datasource/project containing the tables/views")
    String getDatasource();

    @Option(shortName = "t", description = "Tables/view to be validated.")
    String getTable();

    @Option(shortName = "v", description = "Variable to be validated.")
    String getVariable();

}
