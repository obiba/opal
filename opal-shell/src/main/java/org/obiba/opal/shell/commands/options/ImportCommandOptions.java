package org.obiba.opal.shell.commands.options;

import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 * This interface declares the options that may be used with the <code>import</code> command.
 * <p/>
 * The <code>unit</code> option indicates the functional unit The <code>destination</code> option indicates the
 * destination datasource.
 * <p/>
 * The file list option (a list of unparsed file arguments at the end of the command line) indicates one or more
 * directories containing the files to be imported into the datasource.
 *
 * @author cag-dspathis
 */
@CommandLineInterface(application = "import")
public interface ImportCommandOptions extends HelpOption {

  @Option(shortName = "u",
      description =
          "The functional unit. If supplied, imported identifiers must exist in opal, otherwise use 'force' option. " +
              "If no unit is supplied, identifiers are imported as is.")
  String getUnit();

  boolean isUnit();

  @Option(shortName = "d",
      description = "The destination datasource into which the variable catalogue and the participants data will be imported.")
  String getDestination();

  @Option(shortName = "s", description = "Copy all tables from this datasource.")
  String getSource();

  boolean isSource();

  @Option(shortName = "t", description = "Copy specified tables.")
  List<String> getTables();

  boolean isTables();

  @Option(shortName = "a",
      description = "Archive directory. If a relative path is given, it is relative to the functional unit's directory.")
  String getArchive();

  boolean isArchive();

  @Unparsed(name = "FILE")
  List<String> getFiles();

  boolean isFiles();

  @Option(shortName = "f",
      description =
          "Forces participant creation when an unknown participant's identifier is encountered in a functional unit. " +
              "Ignored when no functional unit is specified.")
  boolean isForce();

  @Option(shortName = "i",
      description = "Ignore participants having an unknown identifier in a functional unit. " +
          "Ignored when no functional unit is specified or when participant creation is not allowed.")
  boolean isIgnore();

  @Option(shortName = "c",
      description = "Use incremental import.")
  boolean isIncremental();

  @Option(description = "Create new variables in the destination table if they do not yet exist.")
  boolean isCreateVariables();
}
