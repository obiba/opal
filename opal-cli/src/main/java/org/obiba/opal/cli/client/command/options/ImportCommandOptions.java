package org.obiba.opal.cli.client.command.options;

import java.io.File;
import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 * This interface declares the options that may be used with the <code>import</code> command.
 * 
 * The <code>datasource</code> option indicates the destination datasource.
 * 
 * The <code>owner</code> option indicates the "owner" of any identifiers used in the data to be imported.
 * 
 * The file list option (a list of unparsed file arguments at the end of the command line) indicates one or more
 * directories containing the files to be imported into the datasource.
 * 
 * @author cag-dspathis
 * 
 */
@CommandLineInterface(application = "import")
public interface ImportCommandOptions extends HelpOption {

  @Unparsed(name = "FILE")
  public List<File> getFiles();

  public boolean isFiles();

  @Option(shortName = "d", description = "The destination datasource into which the variable catalogue and the participants data will be imported.")
  public String getDatasource();

  @Option(shortName = "o", description = "The variable name under which the participants identifiers that will be stored in the participants key database.")
  public String getOwner();

}
