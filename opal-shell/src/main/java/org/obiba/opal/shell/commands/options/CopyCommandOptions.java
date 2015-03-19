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
  String getUnit();

  boolean isUnit();

  @Option(shortName = "s", description = "Copy all tables from this datasource.")
  String getSource();

  boolean isSource();

  @Option(shortName = "d", description = "Copy to this existing datasource.")
  String getDestination();

  boolean isDestination();

  @Option(shortName = "o", description = "Copy to file: CSV if file is a directory or file has suffix csv, " +
      "Excel if file has suffix xls or xlsx, XML if file has suffix zip.")
  String getOut();

  boolean isOut();

  //
  // Table transformation
  //
  @Option(shortName = "n",
      description = "Name of the destination table, when only one table is to be copied.")
  String getName();

  boolean isName();

  //
  // Data Query
  //
  @Option(shortName = "q",
      description = "Query to filter the table entities.")
  String getQuery();

  boolean isQuery();

  //
  // Values
  // 

  @Option(longName = "non-incremental", shortName = "i",
      description = "Non-incremental copy (i.e., copy all data not just updates).")
  boolean getNonIncremental();

  @Option(longName = "no-values", shortName = "l", description = "Do not copy the values.")
  boolean getNoValues();

  @Option(longName = "no-variables", shortName = "v", description = "Do not copy the variables.")
  boolean getNoVariables();

  @Option(longName = "copy-null", shortName = "n", description = "Copy null values.")
  boolean getCopyNullValues();

  //
  // Variable transformations
  //

  @Option(shortName = "m",
      description = "Dispatch the variables and values to a table which name is provided by the javascript.")
  String getMultiplex();

  boolean isMultiplex();

  @Option(shortName = "t", description = "Rename each variable with the name provided by the javascript.")
  String getTransform();

  boolean isTransform();

  @Unparsed(name = "TABLE_NAME")
  List<String> getTables();
}
