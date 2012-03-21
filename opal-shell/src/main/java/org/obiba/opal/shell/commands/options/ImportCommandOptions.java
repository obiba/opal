package org.obiba.opal.shell.commands.options;

import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 * This interface declares the options that may be used with the <code>import</code> command.
 * 
 * The <code>unit</code> option indicates the functional unit The <code>destination</code> option indicates the
 * destination datasource.
 * 
 * The file list option (a list of unparsed file arguments at the end of the command line) indicates one or more
 * directories containing the files to be imported into the datasource.
 * 
 * @author cag-dspathis
 * 
 */
@CommandLineInterface(application = "import")
public interface ImportCommandOptions extends HelpOption {

  @Option(shortName = "u", description = "The functional unit. If supplied, imported identifiers must exist in opal, otherwise use 'force' option. If no unit is supplied, identifiers are imported as is.")
  public String getUnit();

  public boolean isUnit();

  @Option(shortName = "d", description = "The destination datasource into which the variable catalogue and the participants data will be imported.")
  public String getDestination();

  @Option(shortName = "s", description = "Copy all tables from this datasource.")
  public String getSource();

  public boolean isSource();

  @Option(shortName = "t", description = "Copy specified tables.")
  public List<String> getTables();

  public boolean isTables();

  @Option(shortName = "a", description = "Archive directory. If a relative path is given, it is relative to the functional unit's directory.")
  public String getArchive();

  public boolean isArchive();

  @Unparsed(name = "FILE")
  public List<String> getFiles();

  public boolean isFiles();

  @Option(shortName = "f", description = "Forces participant creation when an unknown participant's identifier is encountered in a functional unit. Ignored when no functional unit is specified.")
  boolean isForce();
}
