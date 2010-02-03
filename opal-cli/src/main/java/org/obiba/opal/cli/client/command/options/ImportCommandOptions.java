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
public interface ImportCommandOptions extends HelpOption {

  @Unparsed(name = "FILE")
  public List<File> getFiles();

  public boolean isFiles();

  @Option(shortName = "d", description = "Indicates the destination datasource.")
  public String getDatasource();

  @Option(shortName = "o", description = "Indicates the owner of any identifiers used in the data to be imported.")
  public String getOwner();

  @Option(shortName = "e", description = "Indicates that the files to be imported are encrypted.")
  public boolean getEncrypted();
}
