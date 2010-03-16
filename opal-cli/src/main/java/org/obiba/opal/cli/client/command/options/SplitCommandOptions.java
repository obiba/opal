package org.obiba.opal.cli.client.command.options;

import java.io.File;
import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 * This interface declares the options that may be used with the decrypt command.
 * 
 * Note that the <code>getFiles</code> method is used to access unparsed file arguments at the end of the command line.
 * 
 * @author cag-dspathis
 * 
 */
@CommandLineInterface(application = "split")
public interface SplitCommandOptions extends HelpOption {

  @Unparsed(name = "FILE")
  public List<File> getFiles();

  @Option(shortName = "o", longName = "out", description = "The directory into which the decrypted files are written. Default is current directory.")
  public File getOutput();

  @Option(shortName = "s", longName = "size", description = "Maximum number of entities to write per file. Default is 500.", defaultValue = "500")
  public Integer getChunkSize();

}
