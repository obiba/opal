package org.obiba.opal.cli.client.command.options;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;

/**
 * This interface declares the options that may be used with the <code>import</code> command.
 * 
 * The <code>datasource</code> option indicates the destination datasource.
 * 
 * The file list option (a list of unparsed file arguments at the end of the command line) indicates one or more
 * directories containing the files to be imported into the datasource.
 * 
 * @author cag-dspathis
 * 
 */
@CommandLineInterface(application = "import")
public interface ImportCommandOptions extends DatasourceOption, FileListOption, HelpOption {
  // inherited options only
}
