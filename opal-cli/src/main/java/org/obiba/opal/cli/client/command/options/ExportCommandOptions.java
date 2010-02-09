package org.obiba.opal.cli.client.command.options;

import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * This interface declares the options that may be used with the "export" command.
 */
@CommandLineInterface(application = "export")
public interface ExportCommandOptions extends HelpOption {

  @Option(shortName = "d", description = "Export to this existing datasource destination.")
  public String getDestination();

  public boolean isDestination();

  @Option(shortName = "o", description = "Export to this Excel file.")
  public String getOut();

  public boolean isOut();

  @Option(shortName = "t", description = "Fully qualified Magma table names.")
  public List<String> getTables();
}
