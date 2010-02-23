package org.obiba.opal.cli.client.command.options;

import java.io.File;
import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 * This interface declares the options that may be used with the "export" command.
 */
@CommandLineInterface(application = "export")
public interface ExportCommandOptions extends HelpOption {

  @Option(shortName = "d", description = "Export to this existing datasource destination.")
  public String getDestination();

  public boolean isDestination();

  @Option(shortName = "o", description = "Export to this Excel file.")
  public File getOut();

  public boolean isOut();

  @Unparsed(name = "TABLE_NAME")
  public List<String> getTables();
}
