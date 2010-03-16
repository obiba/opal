package org.obiba.opal.shell.commands.options;

import java.io.File;
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

  @Option(shortName = "u", description = "The functional unit.")
  public String getUnit();

  @Option(shortName = "d", description = "The destination datasource into which the variable catalogue and the participants data will be imported.")
  public String getDestination();

  @Unparsed(name = "FILE")
  public List<File> getFiles();

  public boolean isFiles();
}
