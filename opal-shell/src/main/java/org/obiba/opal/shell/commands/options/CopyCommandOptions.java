package org.obiba.opal.shell.commands.options;

import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 * This interface declares the options that may be used with the "export" command.
 */
@CommandLineInterface(application = "copy")
public interface CopyCommandOptions extends HelpOption {

  //
  // In and out datasources
  // 

  @Option(shortName = "u", description = "The functional unit.")
  public String getUnit();

  public boolean isUnit();

  @Option(shortName = "s", description = "Copy all tables from this datasource.")
  public String getSource();

  public boolean isSource();

  @Option(shortName = "d", description = "Copy to this existing datasource.")
  public String getDestination();

  public boolean isDestination();

  @Option(shortName = "o", description = "Copy to a file.")
  public String getOut();

  public boolean isOut();

  //
  // Values
  // 

  @Option(longName = "non-incremental", shortName = "i", description = "Non-incremental copy (i.e., copy all data not just updates).")
  public boolean getNonIncremental();

  @Option(longName = "no-values", shortName = "l", description = "Do not copy the values.")
  public boolean getNoValues();

  @Option(longName = "no-variables", shortName = "v", description = "Do not copy the variables.")
  public boolean getNoVariables();

  //
  // Variable transformations
  //

  @Option(shortName = "m", description = "Dispatch the variables and values to a table which name is provided by the javascript.")
  public String getMultiplex();

  public boolean isMultiplex();

  @Option(shortName = "t", description = "Rename each variable with the name provided by the javascript.")
  public String getTransform();

  public boolean isTransform();

  @Unparsed(name = "TABLE_NAME")
  public List<String> getTables();
}
