package org.obiba.opal.cli.client.command.options;

import java.io.File;
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

  @Option(shortName = "s", description = "Copy from this datasource.")
  public String getSource();

  public boolean isSource();

  @Option(shortName = "d", description = "Copy to this datasource.")
  public String getDestination();

  public boolean isDestination();

  @Option(shortName = "o", description = "Copy to this Excel file.")
  public File getOut();

  public boolean isOut();

  //
  // Values
  // 

  @Option(shortName = "n", description = "Non-incremental copy (i.e., copy all data not just updates).")
  public boolean getNonIncremental();

  @Option(shortName = "c", description = "Copy only the variable catalogue: tables, variables, categories and their attributes.")
  public boolean getCatalogue();

  //
  // Variable transformations
  //

  @Option(shortName = "m", description = "Dispatch the variable and data to a table which name is provided by the javascript.")
  public String getMultiplex();

  public boolean isMultiplex();

  @Option(shortName = "t", description = "Rename each variable with the name provided by the javascript.")
  public String getTransform();

  public boolean isTransform();

  @Unparsed(name = "TABLE_NAME")
  public List<String> getTables();
}
