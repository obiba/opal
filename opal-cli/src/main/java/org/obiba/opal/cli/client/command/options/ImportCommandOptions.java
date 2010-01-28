package org.obiba.opal.cli.client.command.options;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * This interface declares the options that may be used with the <code>import</code> command.
 * 
 * The <code>datasource</code> option indicates the destination datasource.
 * 
 * The <code>encrypted</code> option indicates whether the files to be imported are encrypted.
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
public interface ImportCommandOptions extends DatasourceOption, FileListOption, HelpOption {

  @Option(shortName = "o")
  public String getOwner();

  @Option(shortName = "e")
  public boolean getEncrypted();
}
