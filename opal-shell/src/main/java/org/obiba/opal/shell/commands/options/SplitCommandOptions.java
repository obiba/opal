package org.obiba.opal.shell.commands.options;

import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 * This interface declares the options that may be used with the split command.
 * 
 * Note that the <code>getFiles</code> method is used to access unparsed file arguments at the end of the command line.
 * 
 */
@CommandLineInterface(application = "split")
public interface SplitCommandOptions extends HelpOption {

  @Option(shortName = "u", description = "The functional unit. This is used to resolve filenames and decrypt the input file if necessary.")
  public String getUnit();

  @Option(shortName = "o", longName = "out", description = "The directory into which the decrypted files are written. Directory is created if it does not exist.")
  public String getOutput();

  @Option(shortName = "s", longName = "size", description = "Maximum number of entities to write per file. Default is 500.", defaultValue = "500")
  public Integer getChunkSize();

  @Unparsed(name = "FILE")
  public List<String> getFiles();

}
