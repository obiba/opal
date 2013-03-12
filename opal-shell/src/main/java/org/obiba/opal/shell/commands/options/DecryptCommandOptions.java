package org.obiba.opal.shell.commands.options;

import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 * This interface declares the options that may be used with the decrypt command.
 * <p/>
 * Note that the <code>getFiles</code> method is used to access unparsed file arguments at the end of the command line.
 *
 * @author cag-dspathis
 */
@CommandLineInterface(application = "decrypt")
public interface DecryptCommandOptions extends HelpOption {

  @Option(shortName = "u", description = "The functional unit.")
  public String getUnit();

  public boolean isUnit();

  @Option(shortName = "o", longName = "out",
      description = "The directory into which the decrypted files are written. Default is current directory.")
  public String getOutput();

  public boolean isOutput();

  @Unparsed(name = "FILE")
  public List<String> getFiles();

  public boolean isFiles();
}
